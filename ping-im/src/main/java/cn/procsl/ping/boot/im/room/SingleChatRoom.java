package cn.procsl.ping.boot.im.room;

import cn.procsl.ping.boot.connect.server.Namespace;
import io.socket.socketio.server.SocketIoSocket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Namespace(name = "single")
public class SingleChatRoom {

    @Namespace.Event("message")
    public void chat(SocketIoSocket socket, Object... args) {
        log.info("收到消息:{}, {}", socket, args);
    }

    @Namespace.Connect
    public void connect() {
        log.info("客户端已连接");
    }

    @Namespace.Disconnect
    public void disconnect(SocketIoSocket socket) {
        log.info("客户端已断开连接:{}", socket.getId());
    }

}
