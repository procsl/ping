package cn.procsl.ping.boot.mhpp.protocol.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

@Slf4j
public class HttpConnectResponseHandlerTest {

    @Test
    public void test() throws Exception {
        // 示例1：基本用法
        basicExample();

        // 示例2：高级用法
        advancedExample();
    }

    private static void basicExample() throws InterruptedException {
        HttpConnectClient client = new HttpConnectClient("www.procsl.cn", 8080, "baidu.com", 443);

        Channel channel = client.connect();

        // 等待连接建立
        channel.closeFuture().sync();
    }

    private static void advancedExample() throws InterruptedException {
        AdvancedHttpConnectClient client = new AdvancedHttpConnectClient("localhost", 8080);

        ChannelFuture connectFuture = client.connectThroughProxy("www.baidu.com", 443);
        Channel channel = connectFuture.sync().channel();

        // 添加监听器等待隧道建立
        channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                if (evt instanceof TunnelEstablishedEvent) {
                    log.debug("隧道已建立，可以发送数据");
                    // 示例：发送一些数据
                    String testData = "GET / HTTP/1.1\r\nHost: www.baidu.com\r\n\r\n";
                    ctx.writeAndFlush(Unpooled.copiedBuffer(testData, StandardCharsets.UTF_8));
                }
            }
        });

        // 保持连接
        channel.closeFuture().sync();
        client.shutdown();
    }

}
