package cn.procsl.ping.boot.web.cipher.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
final class CipherRequestUtils {

    final static MimeType ENCRYPT_MIME_TYPE = MimeType.valueOf("application/vnd.enc");

    final static String DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_JSON_VALUE;

    final static String ORIGIN_TYPE_NAME_ENUM = "origin";

    static String parseOriginContentType(String header) {
        MimeType type = MimeType.valueOf(header);

        if (!ENCRYPT_MIME_TYPE.equalsTypeAndSubtype(type)) {
            return header;
        }

        String parameter = type.getParameter(ORIGIN_TYPE_NAME_ENUM);
        if (parameter == null || parameter.isEmpty()) {
            return DEFAULT_CONTENT_TYPE;
        }
        // 需要解码一次, 防止特殊字符导致参数解析失败
        return URLDecoder.decode(parameter, StandardCharsets.UTF_8);
    }



}
