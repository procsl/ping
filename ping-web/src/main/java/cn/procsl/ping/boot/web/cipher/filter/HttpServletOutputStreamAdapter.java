package cn.procsl.ping.boot.web.cipher.filter;

import jakarta.annotation.Nonnull;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Builder
@RequiredArgsConstructor
public final class HttpServletOutputStreamAdapter extends ServletOutputStream {

    private final OutputStream outputStream;
    private final Supplier<Boolean> isReady;
    private final Consumer<WriteListener> setWriteListener;

    @Override
    public boolean isReady() {
        return this.isReady.get();
    }

    @Override
    public void setWriteListener(WriteListener listener) {
        this.setWriteListener.accept(listener);
    }

    @Override
    public void write(int b) throws IOException {
        this.outputStream.write(b);
    }

    @Override
    public void write(@Nonnull byte[] b) throws IOException {
        this.outputStream.write(b);
    }

    @Override
    public void write(@Nonnull byte[] b, int off, int len) throws IOException {
        this.outputStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        this.outputStream.flush();
    }

    //TODO 需要关注是否已经关闭
    @Override
    public void close() throws IOException {
        this.outputStream.close();
    }
}
