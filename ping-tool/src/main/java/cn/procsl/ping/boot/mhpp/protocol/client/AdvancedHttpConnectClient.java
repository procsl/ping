package cn.procsl.ping.boot.mhpp.protocol.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class AdvancedHttpConnectClient {
    private final String proxyHost;
    private final int proxyPort;
    private EventLoopGroup group;

    public AdvancedHttpConnectClient(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    public ChannelFuture connectThroughProxy(String targetHost, int targetPort) {
        this.group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
            .channel(NioSocketChannel.class)
            .handler(new ProxyConnectionInitializer(targetHost, targetPort));

        // 先连接到代理服务器
        return bootstrap.connect(proxyHost, proxyPort);
    }

    public void shutdown() {
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    private static class ProxyConnectionInitializer extends ChannelInitializer<SocketChannel> {
        private final String targetHost;
        private final int targetPort;

        public ProxyConnectionInitializer(String targetHost, int targetPort) {
            this.targetHost = targetHost;
            this.targetPort = targetPort;
        }

        @Override
        protected void initChannel(SocketChannel ch) {

            // HTTP 处理器用于 CONNECT 请求
            ch.pipeline().addLast(new HttpClientCodec());
            ch.pipeline().addLast(new HttpObjectAggregator(8192));

            // 自定义处理器
            ch.pipeline().addLast(new AdvancedConnectHandler(targetHost, targetPort));
            // 添加日志
            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
        }
    }
}
