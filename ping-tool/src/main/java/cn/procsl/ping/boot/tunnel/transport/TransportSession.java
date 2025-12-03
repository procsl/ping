package cn.procsl.ping.boot.tunnel.transport;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface TransportSession extends Closeable {
    InputStream getInput();

    OutputStream getOutput();

    void ping() throws IOException;

    boolean supportsBiDirectional(); // WebSocket/HTTP2 true, SSE false

    TransportStats stats();
}
