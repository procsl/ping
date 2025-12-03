package cn.procsl.ping.boot.mhpp;

import cn.procsl.ping.boot.mhpp.connect.server.HttpConnectServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TunnelServerBootstrap {

    public static void main(String[] args) throws Exception {
        HttpConnectServer server = new HttpConnectServer(8080);
        server.start();
    }

}
