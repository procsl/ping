package cn.procsl.ping.boot.mhpp.connect.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpConnectServer {

    private final int port;

    public HttpConnectServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {

        EventLoopGroup connectorGroup;
        EventLoopGroup workerGroup;
        // 根据环境选择最佳配置
        Class<? extends ServerSocketChannel> channelClass;
        if (Epoll.isAvailable()) {
            connectorGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup();
            channelClass = EpollServerSocketChannel.class;
            log.info("使用 Epoll 高性能模式");
        } else if (KQueue.isAvailable()) {
            connectorGroup = new KQueueEventLoopGroup(1);
            workerGroup = new KQueueEventLoopGroup();
            channelClass = KQueueServerSocketChannel.class;
            log.info("使用 KQueue 高性能模式");
        } else {
            connectorGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            channelClass = NioServerSocketChannel.class;
            log.info("使用 NIO 通用模式");
        }


        try {
            ServerBootstrap proxyServer = new ServerBootstrap();
            proxyServer.group(connectorGroup, workerGroup)
                // 设置服务器通道处理器（处理服务器本身事件）
                .handler(new LoggingHandler(LogLevel.DEBUG))
                // 设置属性（可在 ChannelHandlerContext 中获取）
                .attr(AttributeKey.valueOf("server.name"), "Tunnel-Server/v1.0.0")
                .channel(channelClass)
                .childHandler(new HttpConnectServerInitializer())
                .option(ChannelOption.SO_BACKLOG, 128)
                // 服务器通道选项
                .option(ChannelOption.SO_BACKLOG, 1024)           // 更大的等待队列
                .option(ChannelOption.SO_REUSEADDR, true)         // 端口重用
                // 子通道选项
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)     // 禁用Nagle算法
                .childOption(ChannelOption.SO_RCVBUF, 128 * 1024) // 128K接收缓冲区
                .childOption(ChannelOption.SO_SNDBUF, 128 * 1024) // 128K发送缓冲区
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT); // 内存池

            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                workerGroup.shutdownGracefully();
                connectorGroup.shutdownGracefully();
                log.info("正在关闭HTTP CONNECT 代理服务");
            }));
            // 启动服务
            ChannelFuture f = proxyServer.localAddress("127.0.0.1", port).bind().sync();
            log.info("HTTP CONNECT 代理服务器启动，端口: {}", port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            connectorGroup.shutdownGracefully();
        }
    }


}
