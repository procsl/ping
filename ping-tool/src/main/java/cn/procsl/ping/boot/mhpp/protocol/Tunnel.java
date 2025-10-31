package cn.procsl.ping.boot.mhpp.protocol;

import java.io.IOException;

public interface Tunnel {

    void joinToServer() throws IOException;

    boolean isClosed();

    void close();

    void write(Packet packet) throws Exception;

    Packet read() throws Exception;

}
