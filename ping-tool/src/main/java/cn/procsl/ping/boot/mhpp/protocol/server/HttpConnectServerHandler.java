package cn.procsl.ping.boot.mhpp.protocol.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class HttpConnectServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    private static final HttpResponseStatus SUCCESS = new HttpResponseStatus(200, "Connection Established");
    private static final HttpConnectToRemoteHandler remote = new HttpConnectToRemoteHandler();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        String requestHost = request.headers().get("Host");
        log.info("客户端访问时主机: {}", requestHost);
        if (requestHost == null) {
            this.sendErrorResponse(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        if ("tunnel.procsl.cn".equals(requestHost)) {
            this.sendSuccessResponse(ctx, Unpooled.wrappedBuffer("连接成功".getBytes()));
            return;
        }
        remote.channelRead(ctx, request);
    }


    private void sendSuccessResponse(ChannelHandlerContext ctx, ByteBuf buf) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, SUCCESS, buf);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        String rfc822Time = sdf.format(new Date());
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CONNECTION, "close");
        response.headers().set(HttpHeaderNames.SERVER, "procsl");
        response.headers().set(HttpHeaderNames.DATE, rfc822Time);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    protected void sendErrorResponse(ChannelHandlerContext ctx, HttpResponseStatus status) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        response.headers().set(HttpHeaderNames.SERVER, "procsl");
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        String rfc822Time = sdf.format(new Date());
        response.headers().set(HttpHeaderNames.DATE, rfc822Time);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("连接异常: ", cause);
        ctx.close();
    }
}
