package cn.procsl.ping.boot.mhpp.connect.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class AuthHttpConnectServerHandler extends HttpConnectServerHandler {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // 检查认证头
        if (!authenticate(request)) {
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED);
            response.headers().set("Proxy-Authenticate", "Basic realm=\"Proxy\"");
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }

        // 检查访问控制
        if (!checkAccessControl(request)) {
//            this.sendErrorResponse(ctx, HttpResponseStatus.FORBIDDEN);
            return;
        }

        super.channelRead0(ctx, request);
    }

    private boolean authenticate(FullHttpRequest request) {
        // 实现认证逻辑
        String authHeader = request.headers().get("Proxy-Authorization");
        // 验证 Basic Auth 或其他认证方式
        return true; // 简化示例
    }

    private boolean checkAccessControl(FullHttpRequest request) {
        // 实现访问控制逻辑
        String target = request.uri();
        // 检查目标地址是否在允许列表中
        return true; // 简化示例
    }
}
