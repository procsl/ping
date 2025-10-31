package cn.procsl.ping.boot.mhpp.protocol;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@Slf4j
public class HttpTunnel implements Tunnel {

    private final URL uri;
    private BasePacket reader;
    private boolean close;
    private HttpURLConnection connection;

    public HttpTunnel(String uri) throws IOException {
        this.close = true;
        this.uri = URI.create(uri).toURL();
        this.close = false;
    }

    public boolean isOk() {
        return connection != null && !this.isClosed();
    }

    @Override
    public void joinToServer() throws IOException {
        if (this.isClosed()) {
            throw new IOException("连接已关闭,无法连接服务器");
        }
        this.connection = (HttpURLConnection) this.uri.openConnection();
        if (connection == null) {
            throw new IOException("无法连接服务器");
        }
        connection.setRequestMethod("POST");
        this.connection.setRequestProperty("Connection", "Keep-Alive");
        this.connection.setRequestProperty("Keep-Alive", "timeout=30, max=100");
        this.connection.setRequestProperty("Content-Type", "application/octet-stream");
        this.connection.setRequestProperty("Transfer-Encoding", "chunked");
        this.connection.setRequestProperty("User-Agent", "TunnelClient/java_21;version=1.0.0");
        this.connection.setRequestProperty("Accept", "application/octet-stream");
        this.connection.setRequestProperty("Authorization", "Bearer token");
        connection.setUseCaches(false);

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("状态码错误: " + connection.getResponseCode());
        }

        // 检查响应是否使用 chunked 编码
        String transferEncoding = connection.getHeaderField("Transfer-Encoding");
        if (transferEncoding != null && !transferEncoding.contains("chunked")) {
            throw new IOException("服务器传输方式错误: " + transferEncoding);
        }

        String octet = connection.getHeaderField("Content-Type");
        if (octet != null && !octet.contains("application/octet-stream")) {
            throw new IOException("服务器编码错误: " + octet);
        }
    }

    @Override
    public boolean isClosed() {
        return close;
    }

    @Override
    public void close() {
        if (close) {
            log.info("关闭当前tunnel");
            return;
        }
        close = true;
        if (connection != null) {
            connection.disconnect();
        }
    }

    @Override
    public void write(Packet packet) throws Exception {
        try {
            if (!isOk()) {
                this.joinToServer();
            }
            this.connection.getOutputStream().write(packet.createProtocolBytes(), 0, packet.getLength());
            this.connection.getOutputStream().flush();
        } catch (Exception e) {
            log.warn("出现错误: ", e);
            this.close();
            throw e;
        }
    }


    @Override
    public Packet read() throws IOException {
        try {
            if (!isOk()) {
                this.joinToServer();
            }
            return BasePacket.parserPacket(this.connection.getInputStream());
        } catch (IOException e) {
            this.close();
            log.error("通道出现错误: ", e);
            throw e;
        }
    }


}
