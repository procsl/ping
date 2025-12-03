package cn.procsl.ping.boot.mhpp.connect.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
class HttpConnectToRemoteHandler {


    private static final HttpResponseStatus SUCCESS = new HttpResponseStatus(200, "Connection Established");

    protected void channelRead(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

        // 只处理 CONNECT 方法
        if (!HttpMethod.CONNECT.equals(request.method())) {
            log.info("请求方法错误: {}", request.method());
            sendErrorResponse(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
            return;
        }

        // 解析目标地址
        String[] target = parseTarget(request.uri());
        if (target == null) {
            sendErrorResponse(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        String host = target[0];
        int port = Integer.parseInt(target[1]);
        log.debug("访问目标地址: {}:{}", host, port);


        // 建立到目标服务器的连接
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop())
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    // 目标服务器的处理器 - 简单转发
                    ch.pipeline().addLast(new TargetServerHandler(ctx.channel()));
                }
            });

        ChannelFuture connectFuture = bootstrap.connect(host, port);
        connectFuture.addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                // 连接成功，发送 200 响应
                sendSuccessResponse(ctx);

                // 移除 HTTP 编解码器，切换为原始字节流传输
                if (ctx.pipeline().get("http-codec") != null) {
                    ctx.pipeline().remove("http-codec");
                }
                if (ctx.pipeline().get("aggregator") != null) {
                    ctx.pipeline().remove("aggregator");
                }

                // 添加客户端到代理的转发处理器
                ctx.pipeline().addLast(new ClientToProxyHandler(future.channel()));
            } else {
                sendErrorResponse(ctx, HttpResponseStatus.BAD_GATEWAY);
                ctx.close();
            }
        });
    }

    private String[] parseTarget(String uri) {
        try {
            // URI 格式: host:port
            String[] parts = uri.split(":");
            if (parts.length == 2) {
                return new String[]{parts[0], parts[1]};
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void sendSuccessResponse(ChannelHandlerContext ctx) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1, SUCCESS);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        String rfc822Time = sdf.format(new Date());
        response.headers().set(HttpHeaderNames.DATE, rfc822Time);

        ctx.writeAndFlush(response);
    }

    protected void sendErrorResponse(ChannelHandlerContext ctx, HttpResponseStatus status) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        String rfc822Time = sdf.format(new Date());
        response.headers().set(HttpHeaderNames.DATE, rfc822Time);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


}
