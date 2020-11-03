package io.github.kimmking.gateway.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

public class AddHeaderFilter implements HttpRequestFilter {

    private String headerField;
    private String headerValue;

    public AddHeaderFilter(String headerField, String headerValue) {
        this.headerField = headerField;
        this.headerValue = headerValue;
    }

    @Override
    public void filter(FullHttpRequest fullRequest, ChannelHandlerContext ctx) {
        HttpHeaders headers = fullRequest.headers();
        headers.set(headerField, headerValue);
    }
}
