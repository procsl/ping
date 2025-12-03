package cn.procsl.ping.boot.tunnel.client;

import cn.procsl.ping.boot.tunnel.transport.TransportSession;

import java.io.IOException;

//负责：连接策略、回退排序、重连策略、并发控制、心跳与监控。
//功能：按配置尝试 TransportProvider 列表，找到第一个成功的并将其包装成 StreamTunnel。若主通道失败则自动降级并通知监听器。
public interface StreamTunnelManager {

    TransportSession connect() throws IOException;

}
