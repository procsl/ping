package cn.procsl.ping.boot.mhpp.connect.client;


import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.util.Base64;

public class AuthenticatedConnectHandler extends AdvancedConnectHandler {
    private final String username;
    private final String password;

    public AuthenticatedConnectHandler(String targetHost, int targetPort, String username, String password) {
        super(targetHost, targetPort);
        this.username = username;
        this.password = password;
    }

    @Override
    protected void sendConnectRequest() {
        String target = "";
//        String target = targetHost + ":" + targetPort;
        FullHttpRequest request = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1, HttpMethod.CONNECT, target);

        request.headers().set("Host", target);

        // 添加 Basic 认证
        if (username != null && password != null) {
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            request.headers().set("Proxy-Authorization", "Basic " + encodedAuth);
        }

//        ctx.writeAndFlush(request);
    }
}
