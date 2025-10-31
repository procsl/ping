package cn.procsl.ping.boot.mhpp;

import cn.procsl.ping.boot.mhpp.protocol.server.HttpConnectServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class TunnelServer {

    public static void main(String[] args) throws Exception {
        HttpConnectServer server = new HttpConnectServer(8080);
        server.start();
    }

}
