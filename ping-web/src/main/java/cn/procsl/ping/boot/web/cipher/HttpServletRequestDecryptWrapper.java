package cn.procsl.ping.boot.web.cipher;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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


    Map<String, String[]> currentParameter;

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
            return parseOriginContentType(header);
        }

        return header;
    }

    public static String parseOriginContentType(String header) {
        MimeType type = parseMimeType(header);

        if (type == null) {
            return header;
        }

        if (!ENCRYPT_MIME_TYPE.equalsTypeAndSubtype(type)) {
            return header;
        }

        String parameter = type.getParameter(ORIGIN_TYPE_NAME_ENUM);
        if (parameter == null || parameter.isEmpty()) {
            return defaultContentType;
        }
        // 需要解码一次, 防止特殊字符导致参数解析失败
        return URLDecoder.decode(parameter, StandardCharsets.UTF_8);
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
            String result = parseOriginContentType(tmp.nextElement());
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
        log.info("获取input-stream");
        return super.getInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        log.info("获取reader");
        return super.getReader();
    }

    private static MimeType parseMimeType(String header) {
        try {
            return MimeType.valueOf(header);
        } catch (InvalidMimeTypeException e) {
            log.warn("解析请求头Content-Type失败:", e);
        }
        return null;
    }




}
