package io.github.kimmking.gateway.inbound;

import io.github.kimmking.gateway.filter.AddHeaderFilter;
import io.github.kimmking.gateway.filter.HttpRequestFilter;
import io.github.kimmking.gateway.outbound.okhttp.OkHttpOutboundHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(HttpInboundHandler.class);
    private static final String EXTRA_HEADER_FIELD = "nio";
    private static final String EXTRA_HEADER_VALUE = "zouzhihua";
    private final String proxyServer;
    //private HttpOutboundHandler handler;
    private OkHttpOutboundHandler handler;

    private List<HttpRequestFilter> filters = new ArrayList<>();

    public HttpInboundHandler(String proxyServer) {
        this.proxyServer = proxyServer;
        //handler = new HttpOutboundHandler(this.proxyServer);
        handler = new OkHttpOutboundHandler(this.proxyServer);
        filters.add(new AddHeaderFilter(EXTRA_HEADER_FIELD, EXTRA_HEADER_VALUE));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            //logger.info("channelRead流量接口请求开始，时间为{}", startTime);
            FullHttpRequest fullRequest = (FullHttpRequest) msg;
            for( HttpRequestFilter filter : filters) {
                filter.filter(fullRequest, ctx);
            }
            handler.handle(fullRequest, ctx);

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
