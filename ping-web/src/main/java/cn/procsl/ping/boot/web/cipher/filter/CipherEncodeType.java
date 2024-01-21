package cn.procsl.ping.boot.web.cipher.filter;

import jakarta.annotation.Nonnull;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public enum CipherEncodeType {

    /**
     * base64编码,未加密
     */
    b64,
    /**
     * 加密未编码
     */
    ebin,
    /**
     * base62编码未加密
     */
    b62,
    /**
     * base64编码且加密
     */
    eb64,
    /**
     * 未加密且未编码
     */
    org;


    final public static String HTTP_CONTENT_TYPE_ENUM = "content-type";
    final public static String ENCRYPT_MIME_TYPE_VALUE = "application/vnd.enc";
    final public static MimeType ENCRYPT_MIME_TYPE = MimeType.valueOf(ENCRYPT_MIME_TYPE_VALUE);

    final public static String DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_JSON_VALUE;

    final public static String ORIGIN_TYPE_NAME_ENUM = "origin";
    final public static String ENCODER_TYPE_NAME_ENUM = "encoder";

    public static MimeType createEncryptionContentType(String type, CipherEncodeType encodeType) {
        String origin = URLEncoder.encode(type, StandardCharsets.UTF_8);
        HashMap<String, String> parameters = new HashMap<>(2);
        parameters.put(ORIGIN_TYPE_NAME_ENUM, origin);
        parameters.put(ENCODER_TYPE_NAME_ENUM, encodeType.toString());
        return new MimeType(ENCRYPT_MIME_TYPE, parameters);
    }

    public static String parseOriginContentType(@Nonnull MimeType mimeType) {

        String parameter = mimeType.getParameter(ORIGIN_TYPE_NAME_ENUM);
        if (parameter == null || parameter.isEmpty()) {
            return DEFAULT_CONTENT_TYPE;
        }
        // 需要解码一次, 防止特殊字符导致参数解析失败
        return URLDecoder.decode(parameter, StandardCharsets.UTF_8);
    }

    public static boolean isContentType(String name) {
        return HTTP_CONTENT_TYPE_ENUM.equalsIgnoreCase(name);
    }


}
