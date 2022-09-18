package cn.procsl.ping.boot.connect.server;

import io.socket.socketio.server.SocketIoNamespace;

public interface SocketIONamespaceAware {

    void setSocketIoNamespace(SocketIoNamespace socketIoNamespace);

}
