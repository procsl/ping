package cn.procsl.ping.boot.mhpp.connect.client;


import io.netty.channel.*;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class HttpConnectResponseHandler extends SimpleChannelInboundHandler<HttpObject> {

    private ChannelPromise connectPromise;

    public ChannelFuture getConnectFuture(Channel channel) {
        connectPromise = channel.newPromise();
        return connectPromise;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof HttpResponse response) {
            log.info("收到代理服务器响应: {}", response.status());

            if (response.status().code() == 200) {
                // CONNECT 成功，切换为隧道模式
                log.info("CONNECT 成功，建立隧道连接");

                // 移除 HTTP 处理器
                ctx.pipeline().remove("httpCodec");
                ctx.pipeline().remove("aggregator");
                ctx.pipeline().remove(this);

                // 添加隧道模式处理器
                ctx.pipeline().addLast(new TunnelModeHandler());

                // 通知连接建立成功
                if (connectPromise != null) {
                    connectPromise.setSuccess();
                }
            } else {
                log.error("CONNECT 失败: {}", response.status());
                if (connectPromise != null) {
                    connectPromise.setFailure(new RuntimeException("CONNECT failed: " + response.status()));
                }
                ctx.close();
            }
        } else {
            throw new IOException("Unexpected HttpResponse: " + msg.getClass());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("连接异常", cause);
        if (connectPromise != null) {
            connectPromise.setFailure(cause);
        }
        ctx.close();
    }
}
