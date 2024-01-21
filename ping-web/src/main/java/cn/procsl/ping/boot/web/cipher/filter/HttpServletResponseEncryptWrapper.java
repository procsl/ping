package cn.procsl.ping.boot.web.cipher.filter;

import cn.procsl.ping.boot.web.cipher.CipherException;
import cn.procsl.ping.boot.web.cipher.CipherLockupService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.util.MimeType;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static cn.procsl.ping.boot.web.cipher.filter.CipherEncodeType.ENCODER_TYPE_NAME_ENUM;

final class HttpServletResponseEncryptWrapper extends HttpServletResponseWrapper {


    private final MimeType mime;
    private ServletOutputStream outputStream;

    private final CipherLockupService cipherLockupService;
    private Cipher cipher;
    private PrintWriter printWriter;

    public HttpServletResponseEncryptWrapper(HttpServletRequest request,
                                             HttpServletResponse response,
                                             CipherLockupService cipherLockupService,
                                             MimeType mime) {
        super(response);
        this.cipherLockupService = cipherLockupService;
        this.mime = mime;
    }


    @Override
    public void setContentType(String type) {
        super.setContentType(CipherEncodeType.createEncryptionContentType(type, CipherEncodeType.ebin).toString());
    }


    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.outputStream != null) {
            return this.outputStream;
        }
        final ServletOutputStream out = super.getOutputStream();
        OutputStream newOut = builderOutputStream(out);
        this.outputStream = HttpServletOutputStreamAdapter.builder()
                .isReady(out::isReady)
                .setWriteListener(out::setWriteListener)
                .outputStream(newOut)
                .build();
        return this.outputStream;
    }

    OutputStream builderOutputStream(ServletOutputStream out) {

        try {
            String encode = mime.getParameter(ENCODER_TYPE_NAME_ENUM);
            CipherEncodeType value = CipherEncodeType.valueOf(encode);
            switch (value) {
                case ebin -> {
                    this.cipher = this.cipherLockupService.lockupEncryptCipher(CipherLockupService.CipherScope.request);
                    return new CipherOutputStream(out, cipher);
                }
                case eb64 -> {
                    this.cipher = this.cipherLockupService.lockupEncryptCipher(CipherLockupService.CipherScope.request);
                    return Base64.getEncoder().wrap(new CipherOutputStream(out, cipher));
                }
                case org -> {
                    return out;
                }
                case b64 -> {
                    return Base64.getEncoder().wrap(out);
                }
                case null, default -> throw new CipherException("不支持的编码格式", null);
            }

        } catch (RuntimeException e) {
            if (e instanceof CipherException) {
                throw e;
            }
            throw new CipherException("响应格式编码失败", e);
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.printWriter != null) {
            return this.printWriter;
        }
        this.printWriter = new PrintWriter(this.getOutputStream(), true, StandardCharsets.UTF_8);
        return this.printWriter;
    }

    void onResponseFinished() {
        if (this.cipher == null) {
            return;
        }
        try {
            this.outputStream.flush();
        } catch (IOException ignored) {
        }
        this.cipherLockupService.release(CipherLockupService.CipherScope.request, this.cipher);
    }


}
