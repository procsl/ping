package io.socket.engineio.server.transport;

import io.socket.engineio.server.parser.Parser;
import io.socket.engineio.server.parser.Packet;
import io.socket.engineio.server.EngineIoWebSocket;
import io.socket.engineio.server.Transport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * WebSocket transport.
 */
public final class WebSocket extends Transport {

    public static final String NAME = "websocket";

    private final EngineIoWebSocket mConnection;

    public WebSocket(EngineIoWebSocket webSocket, Parser parser) {
        super(parser);
        mConnection = webSocket;
        mConnection.on("message", args -> onData(args[0]));
        mConnection.on("close", args -> onClose());
        mConnection.on("error", args -> onError((String) args[0], (String) args[1]));
    }

    @Override
    public Map<String, String> getInitialQuery() {
        return mConnection.getQuery();
    }

    @Override
    public Map<String, List<String>> getInitialHeaders() {
        return mConnection.getConnectionHeaders();
    }

    @Override
    public void onRequest(HttpServletRequest request, HttpServletResponse response) {
    }

    @Override
    public void send(List<Packet<?>> packets) {
        final Parser.EncodeCallback<Object> encodeCallback = data -> {
            try {
                if (data instanceof String) {
                    mConnection.write((String) data);
                } else if (data instanceof byte[]) {
                    mConnection.write((byte[]) data);
                }
            } catch (IOException ex) {
                onError("write error", ex.getMessage());
            }
        };
        for (Packet<?> packet : packets) {
            mParser.encodePacket(packet, true, encodeCallback);
        }
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void doClose() {
        mConnection.close();
    }
}