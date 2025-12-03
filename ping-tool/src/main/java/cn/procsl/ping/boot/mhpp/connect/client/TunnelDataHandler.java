package cn.procsl.ping.boot.mhpp.connect.client;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class TunnelDataHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("隧道数据通道已激活");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof ByteBuf) {
            ByteBuf data = (ByteBuf) msg;

            // 处理从目标服务器返回的数据
            processIncomingData(data);

            // 可以在这里实现各种协议处理逻辑
            // 例如 HTTPS、WebSocket 等
        }
    }

    private void processIncomingData(ByteBuf data) {
        // 示例：打印接收到的数据
        byte[] bytes = new byte[Math.min(data.readableBytes(), 100)];
        data.getBytes(data.readerIndex(), bytes);

        log.info("收到数据: {} 字节", data.readableBytes());
        log.debug("数据预览: {}", new String(bytes, StandardCharsets.US_ASCII));

        // 注意：不要释放 ByteBuf，由 Netty 自动管理
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("隧道数据异常", cause);
        ctx.close();
    }
}
