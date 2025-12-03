package cn.procsl.ping.boot.mhpp.protocol;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;

public class HttpTunnelControlServer {

    public void start() throws Exception {

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8849), 1);
        httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        HttpHandler handler = httpExchange -> {
            httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
            String rfc822Time = sdf.format(new Date());
            httpExchange.getResponseHeaders().add("Date", rfc822Time);
            httpExchange.getResponseHeaders().add("X-Response-Author", "procsl");
            httpExchange.getResponseHeaders().add("Server", "TunnelServer");
            httpExchange.close();
        };
        // 接收消息, GET请求, 一直接收, 直到关闭
        httpServer.createContext("/tunnels", handler);
        // 发送消息, POST请求, 一直发送 直到关闭
//        httpServer.createContext("/tunnels", handler);
        httpServer.start();


    }

    public void receiver(HttpExchange httpExchange) {

    }

}
