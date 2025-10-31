package cn.procsl.ping.boot.mhpp.protocol.server;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
// 目标服务器到客户端的转发
public class TargetServerHandler extends ChannelInboundHandlerAdapter {
    private final Channel clientChannel;

    public TargetServerHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 将目标服务器的数据转发给客户端
        clientChannel.writeAndFlush(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 目标服务器断开连接，同时关闭客户端连接
        if (clientChannel.isActive()) {
            clientChannel.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("连接异常: ", cause);
        ctx.close();
    }
}
