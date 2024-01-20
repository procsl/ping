package cn.procsl.ping.boot.web.cipher.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Builder
@RequiredArgsConstructor
public final class HttpServletInputStreamAdapter extends ServletInputStream {

    private final InputStream inputStream;
    private final Supplier<Boolean> isFinished;
    private final Supplier<Boolean> isReady;
    private final Consumer<ReadListener> setReadListener;

    @Override
    public boolean isFinished() {
        return isFinished.get();
    }

    @Override
    public boolean isReady() {
        return this.isReady.get();
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        this.setReadListener.accept(readListener);
    }


    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public int read(@SuppressWarnings("NullableProblems") byte[] b) throws IOException {
        return inputStream.read(b);
    }

    @Override
    public int read(@SuppressWarnings("NullableProblems") byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return inputStream.readAllBytes();
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        return inputStream.readNBytes(len);
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        return inputStream.readNBytes(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        inputStream.skipNBytes(n);
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public void mark(int readLimit) {
        inputStream.mark(readLimit);
    }

    @Override
    public void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        return inputStream.transferTo(out);
    }
}
