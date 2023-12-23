package cn.procsl.ping.boot.web.cipher;

import cn.procsl.ping.boot.common.utils.CipherFactory;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import java.io.*;
import java.util.*;

/**
 * 完整格式
 * application/vnd.enc;encode=base64;origin=base64
 * application/vnd.enc;encode=binary;origin=base64
 */
@Slf4j
final class HttpServletRequestDecryptWrapper extends HttpServletRequestWrapper {

    final static String ORIGIN_TYPE_NAME_ENUM = "origin";

    final static String ENCODE_TYPE_NAME_ENUM = "encode";

    final static String HTTP_CONTENT_TYPE_ENUM = "content-type";

    final static MimeType ENCRYPT_MIME_TYPE = MimeType.valueOf("application/vnd.enc");

    final static String defaultContentType = MediaType.APPLICATION_JSON_VALUE;

    boolean isBase64 = false;

    Map<String, String[]> currentParameter;

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
    public String getParameter(String name) {
        return super.getParameter(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (this.currentParameter == null) {
            this.parseParameter();
        }

        return this.currentParameter;
    }

    private void parseParameter() {
        String queryString = this.getHttpServletRequest().getQueryString();
        Map<String, String[]> map = super.getParameterMap();
        this.currentParameter = map;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (this.currentParameter == null) {
            this.parseParameter();
        }
        return Collections.enumeration(this.currentParameter.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        if (this.currentParameter == null) {
            this.parseParameter();
        }
        return this.currentParameter.get(name);
    }

    private HttpServletRequest getHttpServletRequest() {
        ServletRequest tmp = super.getRequest();
        return (HttpServletRequest) tmp;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (this.inputStream == null) {
            log.info("获取InputStream");
            ServletInputStream is = super.getInputStream();
            // TODO 需要探测是否有编码, 默认 base64
            InputStream dis = Base64.getDecoder().wrap(is);
            Cipher cipher = CipherFactory.init().build().getCipher();
            CipherInputStream cis = new CipherInputStream(dis, cipher);
            this.inputStream = HttpServletInputStreamAdapter.builder().inputStream(cis).setReadListener(is::setReadListener).isReady(is::isReady).isFinished(() -> {
                try {
                    return cis.available() <= 0 && is.isFinished();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).build();
        }
        return this.inputStream;
    }


    @Override
    public String getCharacterEncoding() {
        String enc = super.getCharacterEncoding();
        return enc != null ? enc : "ISO-8859-1";
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (this.reader == null) {
            log.info("获取reader");
            // TODO 编码需要进一步探测 Content-Type, 默认为 UTF8

            Reader reader = new InputStreamReader(this.getInputStream(), this.getCharacterEncoding());
            // TODO 需要探测 Content-Length, 默认 128
            this.reader = new BufferedReader(reader, 128);
        }
        return this.reader;
    }


}
