package cn.procsl.ping.boot.tcp;

import cn.procsl.ping.boot.tcp.server.TcpPortForwardSever;

public class Bootstrap {

    public static void main(String[] args) {
//        TcpPortForwardSever server = new TcpPortForwardSever(8089, 7777, "127.0.0.1");
//        server.start();

        TcpPortForwardSever server2 = new TcpPortForwardSever(8099, 7777, "127.0.0.1");
        server2.start();
    }

}
