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
