# 作业说明
## Week09 作业题目（周六）：
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

## Week09 作业题目（周日）：

3.（必做）结合 dubbo+hmily，实现一个 TCC 外汇交易处理，代码提交到 GitHub:

用户 A 的美元账户和人民币账户都在 A 库，使用 1 美元兑换 7 人民币 ;
用户 B 的美元账户和人民币账户都在 B 库，使用 7 人民币兑换 1 美元 ;
设计账户表，冻结资产表，实现上述两个本地事务的分布式事务。

（1）实现的思路

单个用户的兑换流程，可以认为是这样一个流程：首先要根据兑换出去的金额和转换的汇率计算出转入的币种的金额；然后同时在传出币种的账户进行转出操作金额是兑换出去的金额，转入币种的账户转入计算后的金额。同时成功则兑换成功，如果转出的金额不足，不能进行转入操作等情况，则兑换交易失败，无论是转出的币种账户还是转入的币种账户均未因交易失败受到损失。这样可以将将整个单用户的兑换交易由下面两种服务协作完成：分别是账户（account）服务，和兑换（exchange）服务。因为是实例代码，简化处理将account又拆成人民币账户服务和美元账户服务。对于账户服务，基本操作是转出和转入，转出需要账户余额（balance）大于转出的金额。兑换服务就是根据汇率分别计算相应账户转出和转入的金额，然后调用人民币账户和美元账户进行相应的转出和转入操作。为了能够整个服务协助过程中转出操作正常进行，需要冻结传出的金额。这里需要说明一下为了便于进行计算，在计算过程中和数据库中金额（元）是放到了10000倍，只保留整数。最后显示时再除以10000（这部分涉及显示，代码中没有处理）。对于兑换的汇率，因为是实例代码，没有将获取汇率的部分单独提出来变成服务，而且写死了人民币兑换美元和美元兑换人民币两个汇率。此外，如果是两个用户直接进行不同币种的交易，实际上是基于两个单个用户分别单独进行兑换的协作，即在上面再加一个发起者，让兑换服务变成参与者。这部分代码并没有进行实现，但实现起来不难。

（2）相关数据库设计和配置

因考虑到账户和账户冻结表需要进行分库，因此将账户库分成两个库accountdb0和accountdb1，账户服务是通过shardingsphere proxy进行连接。分库是根据用户ID模2进行分库，人民币账户表、美元账户表、人民币冻结表和美元冻结表中都包含了用户ID字段，而且账户接口的对于的数据库sql都包含了user_id的判断条件。以下是这四张表的建表语句

```sql
CREATE TABLE `t_cny_account` (
  `id` bigint(20) unsigned NOT NULL COMMENT '账户ID',
  `user_id` bigint(20) unsigned NOT NULL COMMENT '用户标识',
  `balance` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '考虑到汇率计算，金额（元）放到10000倍',
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin

CREATE TABLE `t_cny_freeze` (
  `id` bigint(20) unsigned NOT NULL COMMENT '标识',
  `user_id` bigint(20) NOT NULL COMMENT '用户标识',
  `account_id` bigint(20) NOT NULL COMMENT '人民币账户标识',
  `amount` bigint(20) NOT NULL COMMENT '冻结金额，该数值为金额（元）放大10000倍',
  `exchange_id` bigint(20) NOT NULL COMMENT '关联的兑换交易的标识',
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin


CREATE TABLE `t_usd_account` (
  `id` bigint(20) unsigned NOT NULL COMMENT '账户ID',
  `user_id` bigint(20) unsigned NOT NULL COMMENT '用户标识',
  `balance` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '考虑到汇率计算，金额（元）放到10000倍',
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin

CREATE TABLE `t_usd_freeze` (
  `id` bigint(20) unsigned NOT NULL COMMENT '标识',
  `user_id` bigint(20) NOT NULL COMMENT '用户标识',
  `account_id` bigint(20) NOT NULL COMMENT '美元账户标识',
  `amount` bigint(20) NOT NULL COMMENT '冻结金额，该数值为金额（元）放大10000倍',
  `exchange_id` bigint(20) NOT NULL COMMENT '关联的兑换交易的标识',
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin
```

在ShardingSphere proxy中的配置如下：

```yaml
schemaName: account_db

dataSourceCommon:
 username: root
 password: root
 connectionTimeoutMilliseconds: 30000
 idleTimeoutMilliseconds: 60000
 maxLifetimeMilliseconds: 1800000
 maxPoolSize: 50
 minPoolSize: 1
 maintenanceIntervalMilliseconds: 30000

dataSources:
 db0:
   url: jdbc:mysql://127.0.0.1:3306/accountdb0?serverTimezone=UTC&useSSL=false
 db1:
   url: jdbc:mysql://127.0.0.1:3306/accountdb1?serverTimezone=UTC&useSSL=false

rules:
- !SHARDING
 tables:
   t_cny_account:
     actualDataNodes: db${0..1}.t_cny_account
     keyGenerateStrategy:
       column: id
       keyGeneratorName: snowflake
   t_cny_freeze:
     actualDataNodes: db${0..1}.t_cny_freeze
     keyGenerateStrategy:
       column: id
       keyGeneratorName: snowflake
   t_usd_account:
     actualDataNodes: db${0..1}.t_usd_account
     keyGenerateStrategy:
       column: id
       keyGeneratorName: snowflake
   t_usd_freeze:
     actualDataNodes: db${0..1}.t_usd_freeze
     keyGenerateStrategy:
       column: id
       keyGeneratorName: snowflake
 defaultDatabaseStrategy:
   standard:
     shardingColumn: user_id
     shardingAlgorithmName: database_inline
 defaultTableStrategy:
   none:

 shardingAlgorithms:
   database_inline:
     type: INLINE
     props:
       algorithm-expression: db${user_id % 2}

 keyGenerators:
   snowflake:
     type: SNOWFLAKE
     props:
       worker-id: 123
```

兑换也有兑换的库，其中有兑换记录表。

```sql
CREATE TABLE `t_exchange` (
  `id` bigint(20) unsigned NOT NULL COMMENT '交易记录ID',
  `user_id` bigint(20) unsigned NOT NULL COMMENT '用户ID',
  `out_account_id` bigint(20) unsigned NOT NULL COMMENT '兑换兑出的账户ID',
  `out_currency_type` char(4) NOT NULL COMMENT '货币代码，兑出的币种',
  `out_amount` decimal(10,0) NOT NULL COMMENT '兑出的交易金额',
  `in_account_id` bigint(20) NOT NULL COMMENT '兑换兑入的账户ID',
  `in_currency_type` char(4) NOT NULL COMMENT '货币代码，被兑换成的币种',
  `in_amount` decimal(10,0) NOT NULL COMMENT '兑入的交易总额',
  `status` int(11) NOT NULL COMMENT '状态.0-创建，1-准备，2-兑换成功，3-失败',
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
```

（3）代码实现

实例代码由四个子项目组成，分别是人民币账户服务、美元账户服务、兑换服务、通用服务接口和实体类。其中为了简化实现，账户服务直接拆成人民币账户服务和美元账户服务，控制各自的币种账户和账户冻结表。因为人民币账户服务和美元账户服务大同小异，下面只以人民币账户服务进行说明。

【a】人民币账户服务

对外暴露的RPC调用的接口CnyAccountService，如下

```java
package traincamp.dubbo.hmily.common.account.api;

import org.dromara.hmily.annotation.Hmily;
import traincamp.dubbo.hmily.common.account.entity.AccountChangeDTO;

public interface CnyAccountService {

    Long getAccountId(Long userId);

    @Hmily
    boolean accountDecrease(AccountChangeDTO accountChange);

    @Hmily
    boolean accountIncrease(AccountChangeDTO accountChange);
}
```

这个接口是提供给dubbo调用的接口因此并没有包含在服务的子项目中，是放在common子项目中。而且需要注意到accountDecrease和accountIncrease方法上都添加了@Hmily的注解，这是因为使用Hmily进行分布式事务控制用的。

人民币服务子项目中该接口的实现类CnyAccountServiceImpl，代码如下：

```java
package traincamp.dubbo.cnyaccount.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.hmily.annotation.HmilyTCC;
import org.dromara.hmily.common.exception.HmilyRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import traincamp.dubbo.hmily.common.account.api.CnyAccountService;
import traincamp.dubbo.hmily.common.account.entity.Account;
import traincamp.dubbo.hmily.common.account.entity.AccountChangeDTO;
import traincamp.dubbo.hmily.common.account.entity.AccountFreeze;
import traincamp.dubbo.hmily.common.account.mapper.CnyAccountFreezeMapper;
import traincamp.dubbo.hmily.common.account.mapper.CnyAccountMapper;
import traincamp.dubbo.hmily.common.generator.IdGenerator;

import java.util.Date;

@DubboService(interfaceClass = CnyAccountService.class)
@Component
@Slf4j
public class CnyAccountServiceImpl implements CnyAccountService {

    @Autowired
    private CnyAccountMapper accountMapper;

    @Autowired
    private CnyAccountFreezeMapper accountFreezeMapper;

    private IdGenerator freezeRecordIdGenerator = new IdGenerator(11L);

    @Override
    public Long getAccountId(final Long userId) {
        Account account = accountMapper.findByUserId(userId);
        return account!=null ? account.getId():null ;
    }

    @Override
    @HmilyTCC(confirmMethod = "confirmDecrease", cancelMethod = "cancelDecrease")
    @Transactional(rollbackFor = Exception.class)
    public boolean accountDecrease(final AccountChangeDTO accountChange) {
        log.info("execute CnyAccountServiceImpl.accountDecrease method.the param is " + accountChange.toString());
        int ret = accountMapper.decreaseBalance(accountChange);
        if(0 == ret) {
            throw new HmilyRuntimeException("人民币账户余额不足");
        }
        AccountFreeze accountFreeze = new AccountFreeze();
        accountFreeze.setId(freezeRecordIdGenerator.getId());
        accountFreeze.setAccountId(accountChange.getAccountId());
        accountFreeze.setUserId(accountChange.getUserId());
        accountFreeze.setAmount(accountChange.getChangeAmount());
        accountFreeze.setExchangeId(accountChange.getExchangeId());
        accountFreeze.setCreated(new Date());
        accountFreezeMapper.save(accountFreeze);
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean confirmDecrease(final AccountChangeDTO accountChangeDTO) {
        accountFreezeMapper.deleteByChangeDTO(accountChangeDTO);
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancelDecrease(final AccountChangeDTO accountChangeDTO) {
        int ret = accountMapper.increaseBalance(accountChangeDTO);
        if( 0 == ret) {
            throw new HmilyRuntimeException("恢复冻结人民币资产出错");
        }
        accountFreezeMapper.deleteByChangeDTO(accountChangeDTO);
        return Boolean.TRUE;
    }

    @Override
    @HmilyTCC(confirmMethod = "confirmIncrease", cancelMethod = "cancelIncrease")
    @Transactional(rollbackFor = Exception.class)
    public boolean accountIncrease(final AccountChangeDTO accountChange) {
        log.info("execute CnyAccountServiceImpl.accountIncrease method.the param is " + accountChange.toString());
        int ret = accountMapper.increaseBalance(accountChange);
        if( 0 == ret) {
            throw new HmilyRuntimeException("转入人民币资产出错");
        }
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean confirmIncrease(final AccountChangeDTO accountChangeDTO) {
        log.info("execute CnyAccountServiceImpl.confirmIncrease method.");
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancelIncrease(final AccountChangeDTO accountChangeDTO) {
        log.info("execute CnyAccountServiceImpl.cancelIncrease method.");
        int ret = accountMapper.decreaseBalance(accountChangeDTO);
        if(0 == ret) {
            throw new HmilyRuntimeException("人民币账户余额不足或没有相应账户，无法进行取消转入操作");
        }
        return Boolean.TRUE;
    }
}
```

这个类，要注意类上的注解@DubboService(interfaceClass = CnyAccountService.class)，采用了Dubbo注解的方式。accountDecrease这个方法上有Hmily的注解 @HmilyTCC(confirmMethod = "confirmDecrease", cancelMethod = "cancelDecrease")，指明了confirm方法调用confirmDecrease，cancel方法是cancelDecrease，而accountDecrease方法本身是try方法。另外还有一个注解是事务的注解 @Transactional。在调用到accountDecrease方法时，会将人民币账户扣减相应的金额，如果扣减失败（即update的影响条数为0），则主动抛出异常，事务会回滚。如果扣减成功则在人民币资产冻结表中插入一条数据。当confirm方法调用时，只需删除人民币资产冻结表中的相应记录。当cancel方法调用时会将扣减的资产添加回人民币账户进行冲正操作，然后删除人民币资产冻结表中的相应记录。accountIncrease方法上可以看到同样有HmilyTCC的注解，这个方法是try方法的实现，就是给人民币账户增加指定金额。confirmIncrease是对应的confirm方法，无需相关操作。cancelIncrease是对应的cancel方法，实现是反向扣减相应金额。

现在再看一下，相应的数据库操作的Mapper。这里采用的是Mybatis实现，相应的mapper的接口在common子项目中

```java
@Mapper
public interface CnyAccountMapper {

    @Update("update t_cny_account set balance = balance - #{changeAmount}, updated = now() " +
            " where user_id = #{userId} and  balance - #{changeAmount} >= 0 ")
    int decreaseBalance(AccountChangeDTO accountChangeDTO);

    @Update("update t_cny_account set balance = balance + #{changeAmount}, updated = now() " +
            " where user_id = #{userId} and  #{changeAmount} > 0 ")
    int increaseBalance(AccountChangeDTO accountChangeDTO);

    @Select("select id,user_id,balance, created, updated from t_cny_account where user_id =#{userId} limit 1")
    Account findByUserId(Long userId);

    @Select("select id,user_id,balance, created, updated from t_cny_account where id =#{id} limit 1")
    Account findById(Long id);
}
```



```java
package traincamp.dubbo.hmily.common.account.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import traincamp.dubbo.hmily.common.account.entity.AccountChangeDTO;
import traincamp.dubbo.hmily.common.account.entity.AccountFreeze;

public interface CnyAccountFreezeMapper {

    @Insert(" insert into `t_cny_freeze` (id,user_id,account_id,amount,exchange_id,created) " +
            " values ( #{id},#{userId},#{accountId},#{amount},#{exchangeId},#{created})")
    int save(AccountFreeze accountFreeze);

    @Delete("delete from `t_cny_freeze` where id = #{id} and user_id= #{userId}")
    int deleteById(AccountFreeze accountFreeze);

    @Delete("delete from `t_cny_freeze` where exchange_id = #{exchangeId} and user_id= #{userId}")
    int deleteByChangeDTO(AccountChangeDTO accountChangeDTO);

}
```

人民币账户服务的启动类，代码如下


```java
package traincamp.dubbo.cnyaccount;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableDubboConfig
@EnableDubbo
@MapperScan("traincamp.dubbo.hmily.common.account.mapper")
@EnableTransactionManagement
public class CnyAccountApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(CnyAccountApplication.class);
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        springApplication.run(args);
    }

}
```

注意相关注解的使用，首先需要去除Mongo的配置相关类的导入，需要添加有关Dubbo配置的注解，另外需要指定Mybatis的扫描路径以及开启事务管理。最后要注意main函数中springApplication.setWebApplicationType(WebApplicationType.NONE);这句，表明不是web应用。

最后在列出Hmily和子项目相关的配置文件。首先是Hmily配置文件hmily.yml，这个文件放在resource目录下，内容如下：

```yaml
hmily:
  server:
    configMode: local
    appName: cnyaccount-service
  #  如果server.configMode eq local 的时候才会读取到这里的配置信息.
  config:
    appName: cnyaccount-service
    serializer: kryo
    contextTransmittalMode: threadLocal
    scheduledThreadMax: 16
    scheduledRecoveryDelay: 60
    scheduledCleanDelay: 60
    scheduledPhyDeletedDelay: 600
    scheduledInitDelay: 30
    recoverDelayTime: 60
    cleanDelayTime: 180
    limit: 200
    retryMax: 10
    bufferSize: 8192
    consumerThreads: 16
    asyncRepository: true
    autoSql: true
    phyDeleted: true
    storeDays: 3
    repository: mysql

repository:
  database:
    driverClassName: com.mysql.jdbc.Driver
    url : jdbc:mysql://127.0.0.1:3306/hmily?useUnicode=true&characterEncoding=utf8
    username: root
    password: root
    maxActive: 20
    minIdle: 10
    connectionTimeout: 30000
    idleTimeout: 600000
    maxLifetime: 1800000
  file:
    path: D:\hmilyLog
    prefix: /hmily
  zookeeper:
    host: localhost:2181
    sessionTimeOut: 1000000000
    rootPath: /hmily
```

配置文件application.properties的内容如下：

```properties
#配置端口
server.port=9011

#spring.dubbo.application.name=dubbo-provider
#spring.dubbo.application.registry=zookeeper://127.0.0.1:2181
dubbo.application.name=cnyaccount-service
#dubbo.application.registry=zookeeper://127.0.0.1:2181
dubbo.registry.address=zookeeper://127.0.0.1:2181
dubbo.consumer.timeout=100000
### dubbo 的名称
dubbo.protocol.name=dubbo
### dubbo 的端口（-1 表示随机端口号）
dubbo.protocol.port=-1

spring.datasource.url=jdbc:mysql://127.0.0.1:3307/account_db?useUnicode=true&characterEncoding=utf8&useSSL=false
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=root

#mybatis.type-aliases-package=traincamp.dubbo.hmily.common.account.entity
mybatis.configuration.map-underscore-to-camel-case=true

hmily.support.rpc.annotation = true 
```

配置文件中，主要是dubbo的配置，数据库的配置，以及mybatis的配置和hmily的配置，其中数据库因为分库的原因采用了ShardingSphere Proxy，所以数据库的url是3307的逻辑数据库（实际是3306上有两个账户数据库）。hmily的配置是根据hmily官网说明添加的。

【b】兑换服务

兑换服务的相关接口，有三个，ExchangeService是对外提供使用的接口，目前是在兑换服务的Controller中使用；ExchangeRateService是提供汇率的接口，ExchangeOperationService是进行单用户兑换的操作的服务接口，如果以后两个用户之间进行兑换交易该接口和实现无需改动。以下是三个接口的代码

```java
package traincamp.dubbo.exchange.service;

import traincamp.dubbo.hmily.common.exchange.entity.ExchangeTradeDO;

public interface ExchangeService {
    ExchangeTradeDO exchangeCny2Usd(Long userId, Long amount);
    ExchangeTradeDO exchangeUsd2Cny(Long userId, Long amount);
}
```

```java
package traincamp.dubbo.exchange.service;

import java.math.BigDecimal;

public interface ExchangeRateService {
    BigDecimal getExchangeRate(String outCurrencyType, String inCurrencyType);
}
```

```java
package traincamp.dubbo.exchange.service;

import org.dromara.hmily.annotation.Hmily;
import traincamp.dubbo.hmily.common.exchange.entity.ExchangeTradeDO;

public interface ExchangeOperationService {

    @Hmily
    boolean doExchangeOperation(ExchangeTradeDO exchangeTrade);
}
```

可以看到ExchangeOperationService使用了@Hmily的注解

下面是ExchangeService的实现

```java
package traincamp.dubbo.exchange.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import traincamp.dubbo.exchange.constant.ExchangeConstant;
import traincamp.dubbo.exchange.service.ExchangeOperationService;
import traincamp.dubbo.hmily.common.account.api.CnyAccountService;
import traincamp.dubbo.hmily.common.account.api.UsdAccountService;
import traincamp.dubbo.hmily.common.account.entity.AccountChangeDTO;
import traincamp.dubbo.exchange.service.ExchangeService;
import traincamp.dubbo.exchange.service.ExchangeRateService;
import traincamp.dubbo.hmily.common.exchange.entity.ExchangeTradeDO;
import traincamp.dubbo.hmily.common.exchange.mapper.ExchangeMapper;
import traincamp.dubbo.hmily.common.generator.IdGenerator;

import java.math.BigDecimal;

@Service
@Slf4j
public class ExchangeServiceImpl implements ExchangeService {

    @DubboReference
    private CnyAccountService cnyAccountService;

    @DubboReference
    private UsdAccountService usdAccountService;

    private IdGenerator idGenerator = new IdGenerator(ExchangeConstant.EXCHANGE_WORKER);

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ExchangeOperationService exchangeOperationService;

    @Autowired
    private ExchangeMapper exchangeMapper;

    private final static BigDecimal AMPLIFY_FACTOR = new BigDecimal(10_000);

    @Override
    @Transactional
    public ExchangeTradeDO exchangeCny2Usd(final Long userId, final Long amount) {
        ExchangeTradeDO exchangeTrade = new ExchangeTradeDO();
        Long cnyAccountId = cnyAccountService.getAccountId(userId);
        if (null == cnyAccountId) {
            return exchangeTrade;
        }
        Long usdAccountId = usdAccountService.getAccountId(userId);
        if (null == usdAccountId) {
            return exchangeTrade;
        }
        BigDecimal rate = exchangeRateService.getExchangeRate(ExchangeConstant.CURRENCY_CNY, ExchangeConstant.CURRENCY_USD);
        if (null == rate) {
            return exchangeTrade;
        }
        generateExchangeTrade(userId, cnyAccountId, usdAccountId,
                ExchangeConstant.CURRENCY_CNY, ExchangeConstant.CURRENCY_USD, exchangeTrade);
        fillAmountWithCalculateExchangeRate(amount, rate, exchangeTrade);
        exchangeMapper.save(exchangeTrade);
        exchangeOperationService.doExchangeOperation(exchangeTrade);
        return exchangeTrade;
    }

    private ExchangeTradeDO generateExchangeTrade(final Long userId, final Long outAccountId, final Long inAccountId,
                                                  final String outCurrencyType, final String inCurrencyType,
                                                  final ExchangeTradeDO exchangeTrade) {
        exchangeTrade.setId(idGenerator.getId());
        exchangeTrade.setUserId(userId);
        exchangeTrade.setOutAccountId(outAccountId);
        exchangeTrade.setOutCurrencyType(outCurrencyType);
        exchangeTrade.setInAccountId(inAccountId);
        exchangeTrade.setInCurrencyType(inCurrencyType);
        exchangeTrade.setStatus(ExchangeConstant.STATUS_CREATE);
        return exchangeTrade;
    }

    private void fillAmountWithCalculateExchangeRate(final Long amount, final BigDecimal rate,
                                                        final ExchangeTradeDO exchangeTrade) {
        BigDecimal outAmountValue = new BigDecimal(amount).multiply(AMPLIFY_FACTOR);
        exchangeTrade.setOutAmount(outAmountValue.longValue());
        BigDecimal inAmountValue = outAmountValue.multiply(rate);
        exchangeTrade.setInAmount(inAmountValue.longValue());
    }

    @Override
    public ExchangeTradeDO exchangeUsd2Cny(final Long userId, final Long amount) {
        ExchangeTradeDO exchangeTrade = new ExchangeTradeDO();
        Long usdAccountId = usdAccountService.getAccountId(userId);
        if (null == usdAccountId) {
            return exchangeTrade;
        }
        Long cnyAccountId = cnyAccountService.getAccountId(userId);
        if (null == cnyAccountId) {
            return exchangeTrade;
        }
        BigDecimal rate = exchangeRateService.getExchangeRate(ExchangeConstant.CURRENCY_USD, ExchangeConstant.CURRENCY_CNY);
        if (null == rate) {
            return exchangeTrade;
        }
        generateExchangeTrade(userId, usdAccountId, cnyAccountId,
                ExchangeConstant.CURRENCY_USD, ExchangeConstant.CURRENCY_CNY, exchangeTrade);
        fillAmountWithCalculateExchangeRate(amount, rate, exchangeTrade);
        exchangeMapper.save(exchangeTrade);
        exchangeOperationService.doExchangeOperation(exchangeTrade);
        return exchangeTrade;
    }
}
```

exchangeCny2Usd这个方法是人民币兑换美元，会先检查该用户的人民币和美元账户是否存在，然后通过ExchangeRateService查询汇率，计算兑换的美元金额。创建一个交易的记录，并保存到数据库。最后调用ExchangeOperationService进行实际的兑换操作。其中为了计算和保存，做了几项限制：1.兑换时必须是按元为单位的正整数，2.计算过程中将金额放大10000倍，因为汇率基本上是小数点后四位。计算结果直接保存，数据库类型是bigint。注：因为简单完成，所以有些代码是重复和粗糙的。

ExchangeRateService的实现目前纯是为了简化实现存在的，实际意义不大。下面是ExchangeOperationService的实现类ExchangeOperationServiceImpl。

```java
package traincamp.dubbo.exchange.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import traincamp.dubbo.exchange.constant.ExchangeConstant;
import traincamp.dubbo.exchange.service.ExchangeOperationService;
import traincamp.dubbo.hmily.common.account.api.CnyAccountService;
import traincamp.dubbo.hmily.common.account.api.UsdAccountService;
import traincamp.dubbo.hmily.common.account.entity.AccountChangeDTO;
import traincamp.dubbo.hmily.common.exchange.entity.ExchangeTradeDO;
import traincamp.dubbo.hmily.common.exchange.mapper.ExchangeMapper;

@Service
@Slf4j
public class ExchangeOperationServiceImpl implements ExchangeOperationService {

    @DubboReference
    private CnyAccountService cnyAccountService;

    @DubboReference
    private UsdAccountService usdAccountService;

    @Autowired
    private ExchangeMapper exchangeMapper;

    @Override
    @HmilyTCC(confirmMethod = "confirmStatus", cancelMethod = "cancelStatus")
    @Transactional
    public boolean doExchangeOperation(final ExchangeTradeDO exchangeTrade) {
        log.info("doExchangeOperation is called");
        log.info(exchangeTrade.toString());
        exchangeMapper.updatePrepareStatus(exchangeTrade);
        AccountChangeDTO accountChangeDTO = new AccountChangeDTO();
        accountChangeDTO.setUserId(exchangeTrade.getUserId());
        accountChangeDTO.setExchangeId(exchangeTrade.getId());
        accountChangeDTO.setChangeAmount(exchangeTrade.getOutAmount());
        accountChangeDTO.setAccountId(exchangeTrade.getOutAccountId());
        //扣减兑出的账户的金额
        if(ExchangeConstant.CURRENCY_CNY.equals(exchangeTrade.getOutCurrencyType())) {
            cnyAccountService.accountDecrease(accountChangeDTO);
        } else {
            usdAccountService.accountDecrease(accountChangeDTO);
        }
        accountChangeDTO.setAccountId(exchangeTrade.getInAccountId());
        accountChangeDTO.setChangeAmount(exchangeTrade.getInAmount());
        //增加兑换回的金额到兑入账户
        if(ExchangeConstant.CURRENCY_CNY.equals(exchangeTrade.getInCurrencyType())) {
            cnyAccountService.accountIncrease(accountChangeDTO);
        } else {
            usdAccountService.accountIncrease(accountChangeDTO);
        }
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean confirmStatus(final ExchangeTradeDO exchangeTrade) {
        log.info("confirmStatus is called");
        exchangeMapper.updateSuccessStatus(exchangeTrade);
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancelStatus(final ExchangeTradeDO exchangeTrade) {
        log.info("cancelStatus is called");
        exchangeMapper.updateFailStatus(exchangeTrade);
        return Boolean.TRUE;
    }
}
```

doExchangeOperation方法上添加了@HmilyTCC(confirmMethod = "confirmStatus", cancelMethod = "cancelStatus")注解。首先doExchangeOperation是try方法，先将交易记录的状态设置为准备状态，然后调用转出账户相关接口的accountDecrease方法，然后是转入账户相关接口的accountIncrease方法。confirm方法是confirmStatus就是讲过交易记录的状态设置为成功状态。cancel方法是cancelStatus，就是将交易记录状态设置为失败。

交易记录的mapper接口如下：

```java
package traincamp.dubbo.hmily.common.exchange.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import traincamp.dubbo.hmily.common.exchange.entity.ExchangeTradeDO;

public interface ExchangeMapper {

    @Insert("insert into `t_exchange` (id,user_id,out_account_id,out_currency_type,out_amount," +
            "in_account_id,in_currency_type,in_amount,status,created)" +
            " values ( #{id},#{userId},#{outAccountId},#{outCurrencyType},#{outAmount}," +
            "#{inAccountId},#{inCurrencyType},#{inAmount},#{status},now())")
    int save(ExchangeTradeDO exchangeTradeDO);

    @Update("update `t_exchange` set status=1, updated = now() where id=#{id} and status=0")
    int updatePrepareStatus(ExchangeTradeDO exchangeTradeDO);


    @Update("update `t_exchange` set status=2, updated = now() where id=#{id} and status=1")
    int updateSuccessStatus(ExchangeTradeDO exchangeTradeDO);

    @Update("update `t_exchange` set status=3, updated = now() where id=#{id} and status=1")
    int updateFailStatus(ExchangeTradeDO exchangeTradeDO);
}
```

Controller类ExchangeController，提供对外的web访问，代码如下：

```java
package traincamp.dubbo.exchange.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import traincamp.dubbo.exchange.service.ExchangeService;
import traincamp.dubbo.hmily.common.exchange.entity.ExchangeTradeDO;

import java.math.BigDecimal;

@RestController
public class ExchangeController {

    @Autowired
    private ExchangeService exchangeService;

    private static final BigDecimal AMPLIFY = new BigDecimal("10000");

    /**
     * 人民币兑美元，这里amount必须是整数，即兑换时，人民币必须是整数元进行操作
     * @param userId
     * @param amount
     * @return
     */
    @RequestMapping("/cny2usd")
    public ExchangeTradeDO exchangeCny2Usd(@RequestParam("uid") Long userId,
                                  @RequestParam("amount")Long amount) {
        return exchangeService.exchangeCny2Usd(userId, amount);
    }

    /**
     * 人民币兑美元，这里amount必须是整数，即兑换时，人民币必须是整数元进行操作
     * @param userId
     * @param amount
     * @return
     */
    @RequestMapping("/usd2cny")
    public ExchangeTradeDO exchangeUsd2Cny(@RequestParam("uid") Long userId,
                                  @RequestParam("amount")Long amount) {
        return exchangeService.exchangeUsd2Cny(userId, amount);
    }
}
```

这个controller就是提供一个简单的包装。而且为了简化只提供人民币兑美元和美元兑人民币的服务访问。

兑换服务的启动类如下：

```java
package traincamp.dubbo.exchange;

import org.apache.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableDubboConfig
@MapperScan("traincamp.dubbo.hmily.common.exchange.mapper")
@EnableTransactionManagement
public class ExchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExchangeApplication.class, args);
    }

}
```

相关注解在前面已经提到了，这里不再说明。另外这个服务是提供web服务的。

下面是配置文件的说明，首先是hmily的配置文件hmily.yml

```yaml
hmily:
  server:
    configMode: local
    appName: exchange-service
  #  如果server.configMode eq local 的时候才会读取到这里的配置信息.
  config:
    appName: exchange-service
    serializer: kryo
    contextTransmittalMode: threadLocal
    scheduledThreadMax: 16
    scheduledRecoveryDelay: 60
    scheduledCleanDelay: 60
    scheduledPhyDeletedDelay: 600
    scheduledInitDelay: 30
    recoverDelayTime: 60
    cleanDelayTime: 180
    limit: 200
    retryMax: 10
    bufferSize: 8192
    consumerThreads: 16
    asyncRepository: true
    autoSql: true
    phyDeleted: true
    storeDays: 3
    repository: mysql

repository:
  database:
    driverClassName: com.mysql.jdbc.Driver
    url : jdbc:mysql://127.0.0.1:3306/hmily?useUnicode=true&characterEncoding=utf8
    username: root
    password: root
    maxActive: 20
    minIdle: 10
    connectionTimeout: 30000
    idleTimeout: 600000
    maxLifetime: 1800000
  file:
    path: D:\hmilyLog
    prefix: /hmily
  zookeeper:
    host: localhost:2181
    sessionTimeOut: 1000000000
    rootPath: /hmily
```

然后是配置文件application.properties

```properties
# 配置端口
server.port=9021

dubbo.application.name=exchange-service
dubbo.registry.address=zookeeper://127.0.0.1:2181

spring.datasource.url=jdbc:mysql://127.0.0.1:3306/exchangedb?useUnicode=true&characterEncoding=utf8&useSSL=false
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.username=root
spring.datasource.password=root

mybatis.configuration.map-underscore-to-camel-case=true

hmily.support.rpc.annotation = true 
```

以上代码放在dubbo_hmiy目录下，下面有四个目录：cnyaccount（人民币账户服务）、usdaccount（美元账户服务）、exchange（兑换服务）和common（接口和 相关实体bean以及mapper的部分）



注以上代码主要参考了Hmily官网中实例代码以及官网说明。