package cn.procsl.ping.boot.mhpp.connect.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
// 客户端到目标服务器的转发
public class ClientToProxyHandler extends ChannelInboundHandlerAdapter {
    private final Channel targetChannel;

    public ClientToProxyHandler(Channel targetChannel) {
        this.targetChannel = targetChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 将客户端的数据转发给目标服务器
        targetChannel.writeAndFlush(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 客户端断开连接，同时关闭目标服务器连接
        if (targetChannel.isActive()) {
            log.debug("客户端断开连接，同时关闭目标服务器连接");
            targetChannel.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("连接异常: ", cause);
        ctx.close();
    }
}
