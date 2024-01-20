package cn.procsl.ping.boot.web.cipher;

import cn.procsl.ping.boot.common.utils.CipherFactory;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MimeType;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;

/**
 * 完整格式
 * application/vnd.enc;encoder=base64;origin=application%2Fjson
 * application/vnd.enc;encoder=binary;origin=text%2Fplan
 */
@Slf4j
final class HttpServletRequestDecryptWrapper extends HttpServletRequestWrapper {


    final static String HTTP_CONTENT_TYPE_ENUM = "Content-Type";

    private ServletInputStream inputStream = null;

    private BufferedReader reader = null;

    public HttpServletRequestDecryptWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getHeader(String name) {

        String header = super.getHeader(name);
        if (header == null || header.isEmpty()) {
            return header;
        }

        // 重新解析 Content-Type 头
        if (HTTP_CONTENT_TYPE_ENUM.equalsIgnoreCase(name)) {
            return CipherRequestUtils.parseOriginContentType(header);
        }

        return header;
    }


    @Override
    public Enumeration<String> getHeaders(String name) {

        Enumeration<String> tmp = super.getHeaders(name);

        if (!HTTP_CONTENT_TYPE_ENUM.equalsIgnoreCase(name)) {
            return tmp;
        }

        if (tmp == null || !tmp.hasMoreElements()) {
            return tmp;
        }

        HashSet<String> set = new HashSet<>();
        do {
            String result = CipherRequestUtils.parseOriginContentType(tmp.nextElement());
            set.add(result);
        } while (tmp.hasMoreElements());

        return Collections.enumeration(set);
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

    private InputStream builderDecodeInputStream(ServletInputStream is) {
        String contentType = super.getHeader(HTTP_CONTENT_TYPE_ENUM);
        try {
            MimeType parser = CipherRequestUtils.parseMimeType(contentType);

            if (parser == null) {
                throw new CipherException("未知的请求体格式", null);
            }

            String encode = parser.getParameter("encoder");

            EncodeType encoder = EncodeType.valueOf(encode);
            switch (encoder) {
                case eb64 -> {
                    Cipher cipher = this.getCipher();
                    InputStream wrap = Base64.getDecoder().wrap(is);
                    return new CipherInputStream(wrap, cipher);
                }
                case ebin -> {
                    Cipher cipher = this.getCipher();
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
            throw new CipherException("请求格式解码失败", e);
        }
    }

    Cipher getCipher() {
        Object attr = this.getAttribute("CURRENT_REQUEST_PRIVATE_KEY");
        if (attr == null) {
            throw new CipherException("解密失败,缺少必要参数", null);
        }
        String privateKey = (String) attr;
        byte[] byt = privateKey.getBytes(StandardCharsets.UTF_8);

        return CipherFactory.init()
                .algorithm("AES")
                .mode("ECB")
                .padding("PKCS5Padding")
                .iv(byt).privateKey(byt).cipherMode(CipherFactory.CipherMode.DECRYPT).build().getCipher();
    }


    boolean finished(InputStream cis, ServletInputStream is) {
        try {
            return cis.available() <= 0 && is.isFinished();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String getCharacterEncoding() {
        String enc = super.getCharacterEncoding();
        return enc != null ? enc : "ISO-8859-1";
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (this.reader == null) {
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
        }
        return this.reader;
    }

    private enum EncodeType {
        b64, ebin, base62, eb64, org
    }

}
