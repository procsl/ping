package cn.procsl.ping.boot.connect.server;

import io.socket.engineio.server.EngineIoServer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@RequestMapping
@RequiredArgsConstructor
public class SocketIOServer implements HandshakeInterceptor, WebSocketHandler {

    final public static String endpoint = "/socket.io/";
    protected static final String ATTRIBUTE_ENGINE_BRIDGE = "engineIo.bridge";
    protected static final String ATTRIBUTE_ENGINE_QUERY = "engineIo.query";
    protected static final String ATTRIBUTE_ENGINE_HEADERS = "engineIo.headers";
    final EngineIoServer engineIoServer;

//    @Hidden
//    @RequestMapping(
//            value = endpoint,
//            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS},
//            headers = "Connection!=Upgrade")
//    public void httpHandler(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        engineIoServer.handleRequest(request, response);
//    }


    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        final SocketIOSpringWebSocket webSocket = new SocketIOSpringWebSocket(session);
        session.getAttributes().put(ATTRIBUTE_ENGINE_BRIDGE, webSocket);
        engineIoServer.handleWebSocket(webSocket);
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message)
            throws Exception {
        log.trace("handle message:{}", message.getPayload());
        ((SocketIOSpringWebSocket) session.getAttributes().get(ATTRIBUTE_ENGINE_BRIDGE))
                .handleMessage(message);
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) throws Exception {
        ((SocketIOSpringWebSocket) session.getAttributes().get(ATTRIBUTE_ENGINE_BRIDGE))
                .handleTransportError(exception);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus)
            throws Exception {
        ((SocketIOSpringWebSocket) session.getAttributes().get(ATTRIBUTE_ENGINE_BRIDGE))
                .afterConnectionClosed(closeStatus);
    }


    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler,
                                   @NonNull Map<String, Object> attributes) throws Exception {

        attributes.put(ATTRIBUTE_ENGINE_QUERY, request.getURI().getQuery());
        attributes.put(ATTRIBUTE_ENGINE_HEADERS, request.getHeaders());
        return true;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler,
                               Exception exception) {

    }
}
