package cn.procsl.ping.boot.tcp;

import cn.procsl.ping.boot.tcp.server.TcpPortForwardSever;

public class Bootstrap {

    public static void main(String[] args) {
        String targetHost = "127.0.0.1";
        int sourcePort = 10000;
        int targetPort = 7777;
        TcpPortForwardSever server = new TcpPortForwardSever(sourcePort, targetPort, targetHost);
        server.start();
    }

}
