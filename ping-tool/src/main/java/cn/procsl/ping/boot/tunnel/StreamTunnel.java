package cn.procsl.ping.boot.tunnel;


import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public interface StreamTunnel extends Closeable {
    InputStream getInputStream();    // 读取远端发来的数据（阻塞）

    OutputStream getOutputStream();  // 写入要发送到远端的数据（可阻塞/带缓冲）

    void setMetadata(Map<String, String> meta); // 可选：会在握手时发送

    TunnelState getState();

    void addListener(TunnelListener listener); // 连接/断开/错误事件
}
