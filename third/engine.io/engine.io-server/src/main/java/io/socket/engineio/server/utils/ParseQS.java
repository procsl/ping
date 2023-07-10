package io.socket.engineio.server.utils;


import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public interface ParseQS {

    static String encode(Map<String, String> query) {
        return query.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) +
                        ((entry.getValue() != null && !entry.getValue().equals("")) ?
                                ("=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)) : ""))
                .reduce((s1, s2) -> s1 + "&" + s2)
                .orElse("");
    }

    static Map<String, String> decode(String qs) {
        return Arrays.stream(qs.split("&"))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(
                        s -> URLDecoder.decode(s[0], StandardCharsets.UTF_8),
                        s -> (s.length > 1) ? URLDecoder.decode(s[1], StandardCharsets.UTF_8) : ""));
    }
}
