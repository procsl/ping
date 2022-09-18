package cn.procsl.ping.boot.connect.server;

import io.socket.engineio.server.EngineIoWebSocket;
import io.socket.engineio.server.utils.ParseQS;
import lombok.Getter;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.procsl.ping.boot.connect.server.SocketIOServer.ATTRIBUTE_ENGINE_HEADERS;
import static cn.procsl.ping.boot.connect.server.SocketIOServer.ATTRIBUTE_ENGINE_QUERY;

class SocketIOSpringWebSocket extends EngineIoWebSocket {


    private final WebSocketSession session;

    @Getter
    private final Map<String, String> query;

    @Getter
    private final Map<String, List<String>> connectionHeaders;

    @SuppressWarnings("unchecked")
    SocketIOSpringWebSocket(WebSocketSession session) {
        this.session = session;
        final String queryString = (String) session.getAttributes().get(ATTRIBUTE_ENGINE_QUERY);
        if (queryString != null) {
            this.query = ParseQS.decode(queryString);
        } else {
            this.query = new HashMap<>();
        }
        this.connectionHeaders = Collections.unmodifiableMap(
                (Map<String, List<String>>) session.getAttributes().get(ATTRIBUTE_ENGINE_HEADERS));
    }

    @Override
    public void write(String message) throws IOException {
        session.sendMessage(new TextMessage(message));
    }

    @Override
    public void write(byte[] message) throws IOException {
        session.sendMessage(new BinaryMessage(message));
    }

    @Override
    public void close() {
        try {
            session.close();
        } catch (IOException ignore) {
        }
    }

    /* WebSocketHandler */
    void afterConnectionClosed(CloseStatus closeStatus) {
        emit("close");
    }

    void handleMessage(WebSocketMessage<?> message) {
        if (message.getPayload() instanceof String || message.getPayload() instanceof byte[]) {
            emit("message", message.getPayload());
        } else {
            throw new RuntimeException(String.format(
                    "Invalid message type received: %s. Expected String or byte[].",
                    message.getPayload().getClass().getName()));
        }
    }

    void handleTransportError(Throwable exception) {
        emit("error", "write error", exception.getMessage());
    }

}
