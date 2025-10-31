package cn.procsl.ping.boot.mhpp.protocol.client;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AdvancedConnectHandler extends ChannelInboundHandlerAdapter {

    private final String targetHost;
    private final int targetPort;
    private ChannelHandlerContext ctx;
    private State state = State.CONNECTING;

    private enum State {
        CONNECTING, TUNNEL_ESTABLISHED, CLOSED
    }

    public AdvancedConnectHandler(String targetHost, int targetPort) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        log.info("已连接到代理服务器，发送 CONNECT 请求");

        // 发送 CONNECT 请求
        sendConnectRequest();
        super.channelActive(ctx);
    }

    protected void sendConnectRequest() {
        String target = targetHost + ":" + targetPort;
        FullHttpRequest request = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1, HttpMethod.CONNECT, target);

        // 设置必要的头信息
        request.headers().set("Host", target);
        request.headers().set("User-Agent", "Netty-Proxy-Client/1.0");
        request.headers().set("Proxy-Connection", "Keep-Alive");

        // 添加认证信息（如果需要）
        // String auth = "user:pass";
        // String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        // request.headers().set("Proxy-Authorization", "Basic " + encodedAuth);

        log.info("发送 CONNECT 请求: {}", target);
        ctx.writeAndFlush(request);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;
            log.info("收到代理响应: {}", response.status());

            if (response.status().code() == 200) {
                handleConnectSuccess(ctx);
            } else {
                handleConnectFailure(response);
            }
        } else if (state == State.TUNNEL_ESTABLISHED) {
            // 隧道模式下的数据处理
            handleTunnelData(msg);
        }
    }

    private void handleConnectSuccess(ChannelHandlerContext ctx) {
        log.info("CONNECT 成功，切换到隧道模式");
        state = State.TUNNEL_ESTABLISHED;

        // 移除 HTTP 处理器
        ctx.pipeline().remove("httpCodec");
        ctx.pipeline().remove("aggregator");
        ctx.pipeline().remove("log");

        // 添加隧道处理器
        ctx.pipeline().addLast(new TunnelDataHandler());

        // 通知外部连接已就绪
        ctx.pipeline().fireUserEventTriggered(new TunnelEstablishedEvent());

        log.info("隧道模式已就绪，可以开始传输数据");
    }

    private void handleConnectFailure(HttpResponse response) {
        log.error("CONNECT 失败: {}", response.status());
        state = State.CLOSED;
        ctx.close();
    }

    private void handleTunnelData(Object msg) {
        // 隧道模式下的数据处理
        if (msg instanceof ByteBuf) {
            ByteBuf data = (ByteBuf) msg;
            log.debug("收到隧道数据: {} 字节", data.readableBytes());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("连接异常", cause);
        ctx.close();
    }

    // 发送数据到目标服务器（隧道模式）
    public void sendData(byte[] data) {
        if (state == State.TUNNEL_ESTABLISHED && ctx != null) {
            ByteBuf buf = ctx.alloc().buffer(data.length);
            buf.writeBytes(data);
            ctx.writeAndFlush(buf);
        }
    }
}

// 隧道建立事件
class TunnelEstablishedEvent {
    // 事件标记
}
