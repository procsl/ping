package cn.procsl.ping.boot.mhpp.connect.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TunnelModeHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("隧道模式已激活，可以开始传输数据");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof ByteBuf buf) {
            log.info("从目标服务器收到数据: {} 字节", buf.readableBytes());

            // 在这里处理从目标服务器返回的数据
            // 例如，如果是 HTTPS，这里会是 TLS 握手数据或加密的 HTTP 数据
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("隧道连接关闭");
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("隧道模式异常", cause);
        ctx.close();
    }
}
