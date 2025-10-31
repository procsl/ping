package cn.procsl.ping.boot.mhpp.protocol.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HttpConnectServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 添加日志
        pipeline.addLast("pre-logger-handler", new LoggingHandler(this.getClass(), LogLevel.DEBUG));
        // HTTP 编解码器
        pipeline.addLast("codec", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));

        // 自定义处理器
        pipeline.addLast("connect-handler", new HttpConnectServerHandler());

    }
}

