package cn.procsl.ping.boot.web.cipher;


import java.io.IOException;

public interface Transform {

    void init(TransformWriter writer);

    void update(byte[] buffer, int offset, int length) throws IOException;

    interface TransformWriter {

        boolean write(int b) throws IOException;


    }

}
