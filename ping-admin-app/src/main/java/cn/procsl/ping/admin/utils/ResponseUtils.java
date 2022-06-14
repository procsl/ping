package cn.procsl.ping.admin.utils;

import cn.procsl.ping.boot.rest.exception.ExceptionCode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
public final class ResponseUtils {

    final static JsonMapper jsonMapper = new JsonMapper();

    final static XmlMapper xmlMapper = new XmlMapper();
    private static String template =
            " <!doctype html>" +
                    " <html lang=\"en\">" +
                    " <head>" +
                    "     <meta charset=\"UTF-8\">" +
                    "     <meta name=\"viewport\"" +
                    "           content=\"width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0\">" +
                    "     <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">" +
                    "     <title>{{title}}</title></head>" +
                    " <body><h3>{{body}}</h3></body>" +
                    " </html>";

    public static void forbidden(HttpServletRequest request, HttpServletResponse response, String code, String message) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        MediaType mediaType = ResponseUtils.selectAcceptType(request);
        assert mediaType != null;
        response.setContentType(mediaType.toString());
        ResponseUtils.createResponse(mediaType, response, HttpStatus.FORBIDDEN, code, message);
    }

    static void createResponse(MediaType mediaType, HttpServletResponse response, HttpStatus httpStatus, String code, String message) {

        ExceptionCode error = new ExceptionCode(String.format("%s%s", httpStatus.value(), code), message);

        try {

            if (mediaType.includes(MediaType.APPLICATION_JSON)) {
                byte[] encode = jsonMapper.writeValueAsBytes(error);
                response.getOutputStream().write(encode);
                return;
            }

            if (mediaType.includes(MediaType.APPLICATION_XML)) {
                byte[] encode = xmlMapper.writeValueAsBytes(error);
                response.getOutputStream().write(encode);
                return;
            }

            if (mediaType.includes(MediaType.TEXT_HTML)) {
                byte[] encode = getHtml(error);
                response.getOutputStream().write(encode);
                return;
            }
            log.warn("未配置的响应类型:{}", mediaType);

        } catch (IOException exception) {
            log.warn("输出响应异常:", exception);
        }

    }

    static byte[] getHtml(ExceptionCode error) throws IOException {
        String html = template;
        html = html.replaceAll("\\{\\{title}}", "错误");
        html = html.replaceAll("\\{\\{body\\}\\}", error.getMessage());
        return html.getBytes(StandardCharsets.UTF_8);
    }

    static MediaType selectAcceptType(HttpServletRequest request) {

        String uri = request.getRequestURI();
        if (uri.endsWith(".xml")) {
            return MediaType.APPLICATION_XML;
        }

        if (uri.endsWith(".json")) {
            return MediaType.APPLICATION_XML;
        }

        String header = request.getHeader("Accept");
        if (ObjectUtils.isEmpty(header)) {
            return MediaType.APPLICATION_JSON;
        }

        try {
            List<MediaType> headers = MediaType.parseMediaTypes(Arrays.asList(header.split(",")));
            for (MediaType mediaType : headers) {

                if (mediaType.includes(MediaType.APPLICATION_JSON)) {
                    return MediaType.APPLICATION_JSON;
                }

                if (mediaType.includes(MediaType.APPLICATION_XML)) {
                    return MediaType.APPLICATION_XML;
                }

                if (mediaType.includes(MediaType.TEXT_HTML)) {
                    return MediaType.TEXT_HTML;
                }

            }
        } catch (InvalidMediaTypeException e) {
            return MediaType.APPLICATION_JSON;
        }


        return MediaType.APPLICATION_JSON;
    }


}
