# 作业说明
## Week08 作业题目（周四）：
3.（必做）改造自定义 RPC 的程序，提交到 GitHub：


尝试将服务端写死查找接口实现类变成泛型和反射；


尝试将客户端动态代理改成 AOP，添加异常处理；


尝试使用 Netty+HTTP 作为 client 端传输方式。

（1）尝试将服务端写死查找接口实现类变成泛型和反射；

修改的地方包括，rpcfx-core中的类RpcfxInvoker，其中RpcfxResponse方法，要通过Class.forName方法获得请求的类的接口类型。然后通过rpcfx-demo-provider下的DemoResolver中的resole方法，直接使用applicationContext.getBean(clazz)根据类型返回响应的bean。最后将原本RpcfxServerApplication中bean配置去掉，直接在相应的service接口实现类添加@Service注解。以下为源码部分：
rpcfx-core中的类RpcfxInvoker
```
package io.kimmking.rpcfx.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.kimmking.rpcfx.api.RpcfxRequest;
import io.kimmking.rpcfx.api.RpcfxResolver;
import io.kimmking.rpcfx.api.RpcfxResponse;
import io.kimmking.rpcfx.exception.RpcfxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class RpcfxInvoker {

    private final static Logger log = LoggerFactory.getLogger(RpcfxInvoker.class);

    private RpcfxResolver resolver;

    public RpcfxInvoker(RpcfxResolver resolver){
        this.resolver = resolver;
    }

    public RpcfxResponse invoke(RpcfxRequest request) {
        RpcfxResponse response = new RpcfxResponse();
        String serviceClass = request.getServiceClass();

        Class klass = null;
        try {
            klass = Class.forName(serviceClass);
        } catch (ClassNotFoundException e) {
            log.warn("{}",e);
            response.setException(new RpcfxException(e));
            response.setStatus(false);
            return response;
        }

        Object service = resolver.resole(klass);

        try {
            Method method = resolveMethodFromClass(service.getClass(), request.getMethod());
            Object result = method.invoke(service, request.getParams()); // dubbo, fastjson,
            // 两次json序列化能否合并成一个
            response.setResult(JSON.toJSONString(result, SerializerFeature.WriteClassName));
            response.setStatus(true);
            return response;
        } catch ( IllegalAccessException | InvocationTargetException e) {

            // 3.Xstream

            // 2.封装一个统一的RpcfxException
            // 客户端也需要判断异常
            e.printStackTrace();
            log.warn("{}",e);
            response.setException(new RpcfxException(e));
            response.setStatus(false);
            return response;
        }
    }

    private Class resolveClassFromString(String className) {
        try {
            Class clazz = Class.forName(className);
            return clazz;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private Method resolveMethodFromClass(Class<?> klass, String methodName) {
        return Arrays.stream(klass.getMethods()).filter(m -> methodName.equals(m.getName())).findFirst().get();
    }

}
```
过rpcfx-demo-provider中的DemoResolver类
```
package io.kimmking.rpcfx.demo.provider;

import io.kimmking.rpcfx.api.RpcfxResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DemoResolver implements RpcfxResolver, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> T resole(Class<T> clazz) {
        return this.applicationContext.getBean(clazz);
    }
}
```
（2）尝试将客户端动态代理改成 AOP，添加异常处理；

这个修改主要集中在rpcfx-core中的Rpcfx类，动态代理该由字节码形式，由ByteBuddy实现。代码如下；
```
package io.kimmking.rpcfx.client;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import io.kimmking.rpcfx.api.RpcfxRequest;
import io.kimmking.rpcfx.api.RpcfxResponse;
import io.kimmking.rpcfx.exception.RpcfxException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class Rpcfx {

    static {
        ParserConfig.getGlobalInstance().addAccept("io.kimmking");
    }

    private static final ConcurrentHashMap serviceObjectMap = new ConcurrentHashMap();

    public static <T> T create(final Class<T> serviceClass, final String url) {
        try {
            T reuslt = (T)serviceObjectMap.putIfAbsent(serviceClass, createByteBuddyDynamicProxy(serviceClass, url));
            if(reuslt == null) {  //第一次时会返回null，需要再获取一次。
                reuslt = (T) serviceObjectMap.get(serviceClass);
            }
            return reuslt;
        } catch (Exception e) {
            throw new RpcfxException(e);
        }
    }

    private static <T>T createByteBuddyDynamicProxy(Class<T> serviceClass, String url) throws Exception {
        return (T) new ByteBuddy().subclass(Object.class)
                .implement(serviceClass)
                .method(ElementMatchers.any())
                .intercept(InvocationHandlerAdapter.of(new Rpcfx.ServiceInvocationHandler(serviceClass, url)))
                .make()
                .load(Rpcfx.class.getClassLoader())
                .getLoaded()
                .getDeclaredConstructor()
                .newInstance();
    }

    public static class ServiceInvocationHandler implements InvocationHandler {
        public static final MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");
        private final Class<?> serviceClass;
        private final String url;
        public ServiceInvocationHandler(Class<?> serviceClass, String url) {
            this.serviceClass = serviceClass;
            this.url = url;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
            RpcfxRequest request = new RpcfxRequest();
            request.setServiceClass(this.serviceClass.getName());
            request.setMethod(method.getName());
            request.setParams(params);

            // 使用netty client
            RpcfxResponse response = NettyRpcClient.rpcCall(request, url);
            if (!response.isStatus()) {
                throw new Throwable(response.getException());
            }
            return JSON.parse(response.getResult().toString());
        }
    }

}
```
统一异常，首先先实现一个自定义异常类，RpcfxException类，如下：

```java
package io.kimmking.rpcfx.exception;

public class RpcfxException extends RuntimeException {

    public RpcfxException() {
        super();
    }

    public RpcfxException(String message) {
        super(message);
    }

    public RpcfxException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcfxException(Throwable cause) {
        super(cause);
    }
}
```

然后在捕获到异常后，重新抛出这个异常，构造时将捕获的异常放入到这个RpcfxException类的构造函数中。

（3）尝试使用 Netty+HTTP 作为 client 端传输方式

新建两个类NettyRpcClient和RpcHttpClientHandler。其中NettyRpcClient是构建Netty客户端，RpcHttpClientHandler是ChannelInboundHandlerAdapter的子类，完成接收响应的部分。以下是相关源码，首先是NettyRpcClient类：

```java
package io.kimmking.rpcfx.client;

import com.alibaba.fastjson.JSON;
import io.kimmking.rpcfx.api.RpcfxRequest;
import io.kimmking.rpcfx.api.RpcfxResponse;
import io.kimmking.rpcfx.exception.RpcfxException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;

import java.net.URI;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyRpcClient {

    static EventLoopGroup workerGroup = new NioEventLoopGroup();
    static Bootstrap bootstrap = null;
    static ThreadPoolExecutor executor;

    static {
        start();
        executor = getThreadPoolExecutor();
    }

    private static void start() {
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(workerGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new HttpResponseDecoder());
                    ch.pipeline().addLast(new HttpRequestEncoder());
                    ch.pipeline().addLast(new RpcHttpClientHandler());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ThreadPoolExecutor getThreadPoolExecutor() {
        int coreSize = Runtime.getRuntime().availableProcessors();
        int maxSize = coreSize * 2;
        BlockingQueue<Runnable> workQuee = new LinkedBlockingQueue<>(500);
        ThreadFactory threaFacorty = new CustomThreaFacorty();
        return new ThreadPoolExecutor(coreSize, maxSize, 1, TimeUnit.MINUTES,
                workQuee, threaFacorty);
    }

    public static void stop() {
        executor.shutdown();
        workerGroup.shutdownGracefully();
    }

    public static RpcfxResponse rpcCall(final RpcfxRequest req, final String url) {
        FutureTask<RpcfxResponse> futureTask = new FutureTask<RpcfxResponse>(() -> request(req, url));
        executor.submit(futureTask);
        try {
            return futureTask.get(1000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RpcfxException(e);
        }
    }

    private static RpcfxResponse request(RpcfxRequest req, String url) {
        try {
            URI uri = new URI(url);
            // Start the client.
            ChannelFuture channelFuture = bootstrap.connect(uri.getHost(), uri.getPort()).sync();

            String reqJson = JSON.toJSONString(req);
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST,
                    uri.toASCIIString(), Unpooled.wrappedBuffer(reqJson.getBytes()));

            // 构建http请求
            request.headers().set(HttpHeaders.Names.HOST, uri.getHost());
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            request.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=UTF-8");
            request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());

            channelFuture.channel().write(request);
            channelFuture.channel().flush();
            channelFuture.channel().closeFuture().sync();
            AttributeKey<String> key = AttributeKey.valueOf(RpcClientConstant.RPC_CLIENT_SERVER_DATA);
            Object result = channelFuture.channel().attr(key).get();
            return JSON.parseObject(result.toString(), RpcfxResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class CustomThreaFacorty implements ThreadFactory {
        private AtomicInteger serial = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(false);
            thread.setName("ClientWorkThread-" + serial.getAndIncrement());
            return thread;
        }
    }
}
```

注：使用线程池方式。需要程序退出前可以调用stop方法关闭线程池和netty的workgroup。

接下来是RpcHttpClientHandler类：

```java
package io.kimmking.rpcfx.client;

import com.alibaba.fastjson.JSON;
import io.kimmking.rpcfx.api.RpcfxResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.util.AttributeKey;


public class RpcHttpClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {

        if(msg instanceof HttpContent)
        {
            HttpContent content = (HttpContent)msg;
            ByteBuf buf = content.content();
            System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));
            String respJson = buf.toString(io.netty.util.CharsetUtil.UTF_8);
            buf.release();
            AttributeKey<String> key = AttributeKey.valueOf(RpcClientConstant.RPC_CLIENT_SERVER_DATA);
            ctx.channel().attr(key).set(respJson);
            ctx.channel().close();
        }
    }


}
```

注：本题的代码在rpcfx目录下