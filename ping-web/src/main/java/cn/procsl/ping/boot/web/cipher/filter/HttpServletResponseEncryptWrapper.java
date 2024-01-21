package cn.procsl.ping.boot.web.cipher.filter;

import cn.procsl.ping.boot.web.cipher.CipherLockupService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.util.MimeType;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static cn.procsl.ping.boot.web.cipher.filter.CipherRequestUtils.ENCRYPT_MIME_TYPE;

final class HttpServletResponseEncryptWrapper extends HttpServletResponseWrapper {


    private ServletOutputStream outputStream;

    private final CipherLockupService cipherLockupService;
    private Cipher cipher;
    private PrintWriter printWriter;


    public HttpServletResponseEncryptWrapper(HttpServletRequest request,
                                             HttpServletResponse response,
                                             CipherLockupService cipherLockupService) {
        super(response);
        this.cipherLockupService = cipherLockupService;
    }


    @Override
    public void setContentType(String type) {
        String origin = URLEncoder.encode(type, StandardCharsets.UTF_8);
        HashMap<String, String> parameters = new HashMap<>(2);
        parameters.put(CipherRequestUtils.ORIGIN_TYPE_NAME_ENUM, origin);
        parameters.put(CipherRequestUtils.ENCODER_TYPE_NAME_ENUM, CipherEncodeType.eb64.toString());
        MimeType newType = new MimeType(ENCRYPT_MIME_TYPE, parameters);
        super.setContentType(newType.toString());
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (this.outputStream != null) {
            return this.outputStream;
        }
        final ServletOutputStream out = super.getOutputStream();
        this.cipher = this.cipherLockupService.lockupEncryptCipher(CipherLockupService.CipherScope.request);
        this.outputStream = HttpServletOutputStreamAdapter.builder()
                .isReady(out::isReady)
                .setWriteListener(out::setWriteListener)
                .outputStream(new CipherOutputStream(out, cipher))
                .build();
        return this.outputStream;
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
