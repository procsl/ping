package cn.procsl.ping.boot.tcp.server;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TcpPortForwardSever {

    private final int sourcePort;
    private final int targetPort;
    private final String targetHost;

    public void start() {
        Vertx vertx = Vertx.vertx();
        NetServerOptions serverOptions = new NetServerOptions().setPort(sourcePort).setLogActivity(false);
        NetClientOptions clientOptions = new NetClientOptions().setConnectTimeout(1000).setLogActivity(false);

        NetServer server = vertx.createNetServer(serverOptions);

        server.connectHandler((serverSocket) -> {
            log.info("new connected: {}", serverSocket.remoteAddress());
            // 先暂停
            serverSocket.pause();

            NetClient client = vertx.createNetClient(clientOptions);

            Future<NetSocket> future = client.connect(targetPort, targetHost);
            future.onSuccess(clientSocket -> {
                log.info("tcp forward to {} <===> {}", serverSocket.remoteAddress(), clientSocket.remoteAddress());
                // 写入
                clientSocket.handler(serverSocket::write);
                clientSocket.exceptionHandler(err -> {
                    log.error("Connect to Client failed", err);
                    serverSocket.close();
                });
                clientSocket.closeHandler((handler) -> {
                    log.info("client socket closed: {}", clientSocket.localAddress());
                    serverSocket.close();
                });
                // 重新绑定
                serverSocket.handler(clientSocket::write);
                // 恢复
                serverSocket.resume();
            });

            future.onFailure(fail -> {
                serverSocket.close();
                log.error("Connect to Server failed", future.cause());
            });

            // 读取
            serverSocket.closeHandler(handler -> {
                log.info("server socket closed: {}", serverSocket.remoteAddress());
                client.close();
            });

            serverSocket.exceptionHandler((err) -> {
                log.error("Connect to Server failed", err);
                client.close();
            });

        });

        server.listen(res -> {
            if (res.succeeded()) {
                log.info("Server is now listening on actual port: {}", server.actualPort());
            } else {
                log.error("Failed to bind!");
            }
        });
    }


}
