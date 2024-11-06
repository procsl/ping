package cn.procsl.ping.boot.tcp;

import cn.procsl.ping.boot.tcp.server.TcpPortForwardSever;

public class Bootstrap {

    public static void main(String[] args) {
        String targetHost = "0.0.0.0";
        int sourcePort = 7777;
        int targetPort = 6666;
        TcpPortForwardSever server = new TcpPortForwardSever(sourcePort, targetPort, targetHost);
        server.start();
    }

}
