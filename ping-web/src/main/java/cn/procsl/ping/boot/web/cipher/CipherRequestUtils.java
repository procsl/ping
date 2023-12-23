package cn.procsl.ping.boot.web.cipher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static cn.procsl.ping.boot.web.cipher.HttpServletRequestDecryptWrapper.*;

@Slf4j
public final class CipherRequestUtils {

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


    private static MimeType parseMimeType(String header) {
        try {
            return MimeType.valueOf(header);
        } catch (InvalidMimeTypeException e) {
            log.warn("解析请求头Content-Type失败:", e);
        }
        return null;
    }


}
