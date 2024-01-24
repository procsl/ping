package cn.procsl.ping.boot.web.cipher.filter;

import cn.procsl.ping.boot.web.cipher.CipherException;
import cn.procsl.ping.boot.web.cipher.CipherLockupService;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.MimeType;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.*;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;

import static cn.procsl.ping.boot.web.cipher.filter.CipherEncodeType.*;

/**
 * 完整格式
 * application/vnd.enc;encoder=base64;origin=application%2Fjson
 * application/vnd.enc;encoder=binary;origin=text%2Fplan
 */
final class HttpServletRequestDecryptWrapper extends HttpServletRequestWrapper {


    private ServletInputStream inputStream = null;

    private BufferedReader reader = null;

    private final CipherLockupService cipherLockupService;

    private final MimeType encryptionType;

    private Cipher cipher;

    public HttpServletRequestDecryptWrapper(HttpServletRequest request,
                                            CipherLockupService cipherLockupService, MimeType mime) {
        super(request);
        this.cipherLockupService = cipherLockupService;
        this.encryptionType = mime;
    }

    @Override
    public String getHeader(String name) {

        String header = super.getHeader(name);
        if (header == null || header.isEmpty()) {
            return header;
        }

        // 重新解析 Content-Type 头
        if (isContentType(name)) {
            return parseOriginContentType(encryptionType);
        }

        return header;
    }

    @Override
    public String getContentType() {
        return parseOriginContentType(encryptionType);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {

        Enumeration<String> tmp = super.getHeaders(name);

        if (!isContentType(name)) {
            return tmp;
        }

        return Collections.enumeration(Collections.singleton(parseOriginContentType(this.encryptionType)));
    }


    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (this.inputStream != null) {
            return this.inputStream;
        }
        ServletInputStream is = super.getInputStream();

        InputStream decoder = this.builderDecodeInputStream(is);

        this.inputStream = HttpServletInputStreamAdapter.builder()
                .inputStream(decoder)
                .setReadListener(is::setReadListener)
                .isReady(is::isReady)
                .isFinished(() -> finished(decoder, is)).build();
        return this.inputStream;
    }

    InputStream builderDecodeInputStream(ServletInputStream is) {
        try {
            String encode = encryptionType.getParameter(ENCODER_TYPE_NAME_ENUM);

            CipherEncodeType encoder = CipherEncodeType.valueOf(encode);
            switch (encoder) {
                case eb64 -> {
                    this.cipher = this.cipherLockupService.lockupDecryptCipher(CipherLockupService.CipherScope.request);
                    InputStream wrap = Base64.getDecoder().wrap(is);
                    return new CipherInputStream(wrap, cipher);
                }
                case ebin -> {
                    this.cipher = this.cipherLockupService.lockupDecryptCipher(CipherLockupService.CipherScope.request);
                    return new CipherInputStream(is, cipher);
                }
                case b64 -> {
                    return Base64.getDecoder().wrap(is);
                }
                case org -> {
                    return is;
                }
                default -> throw new CipherException("不支持的解码方式: " + encode, null);
            }
        } catch (RuntimeException e) {
            if (e instanceof CipherException) {
                throw e;
            }
            throw new CipherException("请求格式解码失败", e);
        }
    }


    boolean finished(InputStream cis, ServletInputStream is) {
        try {
            return cis.available() <= 0 && is.isFinished();
        } catch (IOException e) {
            throw new RuntimeException("刷入流失败", e);
        }
    }


    @Override
    public String getCharacterEncoding() {
        String enc = super.getCharacterEncoding();
        return enc != null ? enc : "ISO-8859-1";
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (this.reader != null) {
            return this.reader;
        }

        Reader reader = new InputStreamReader(this.getInputStream(), this.getCharacterEncoding());
        String lenStr = this.getHeader("Content-Length");
        int len = 256;
        if (lenStr != null) {
            try {
                len = Integer.parseInt(lenStr);
                len = len <= 10 ? 64 : len;
                len = len >= 4097 ? 4096 : len;
            } catch (Exception ignored) {
            }
        }
        this.reader = new BufferedReader(reader, len);
        return this.reader;
    }

    /**
     * 由于是加密解密, 因此此时无法计算请求体长度
     *
     * @return -1
     * @see ServletRequest#getContentLength()
     */
    @Override
    public int getContentLength() {
        return -1;
    }

    /**
     * 由于是加密解密, 因此此时无法计算请求体长度
     *
     * @return -1
     * @see ServletRequest#getContentLengthLong
     */
    @Override
    public long getContentLengthLong() {
        return -1L;
    }

    /**
     * 当请求完成时
     */
    void onRequestFinished() {
        if (this.cipher != null) {
            this.cipherLockupService.release(CipherLockupService.CipherScope.session, this.cipher);
        }
    }


}