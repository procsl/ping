package cn.procsl.ping.boot.mhpp.protocol.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpConnectClient {
    private final String proxyHost;
    private final int proxyPort;
    private final String targetHost;
    private final int targetPort;

    public HttpConnectClient(String proxyHost, int proxyPort, String targetHost, int targetPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    public Channel connect() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        // 添加日志
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        ch.pipeline().addLast("httpCodec", new HttpServerCodec());
                        ch.pipeline().addLast("aggregator", new HttpObjectAggregator(8192));

                        // 第一阶段：处理代理连接建立
                        ch.pipeline().addLast("handler", new HttpConnectResponseHandler());
                    }
                });

            // 连接到代理服务器
            Channel channel = bootstrap.connect(proxyHost, proxyPort).sync().channel();

            // 发送 CONNECT 请求
            sendConnectRequest(channel);

            return channel;
        } catch (Exception e) {
            group.shutdownGracefully();
            throw e;
        }
    }

    private void sendConnectRequest(Channel channel) {
        // 构建 CONNECT 请求
        String target = targetHost + ":" + targetPort;
        FullHttpRequest request = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1, HttpMethod.CONNECT, target);

        request.headers().set("Host", target);
        request.headers().set("User-Agent", "Netty-HttpConnect-Client");

        log.debug("发送 CONNECT 请求到: " + target);
        channel.writeAndFlush(request);
    }
}
