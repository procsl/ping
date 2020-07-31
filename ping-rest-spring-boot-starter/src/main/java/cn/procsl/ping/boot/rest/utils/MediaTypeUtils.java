package cn.procsl.ping.boot.rest.utils;

import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author procsl
 * @date 2020/02/22
 */
public class MediaTypeUtils {

    public static MediaType createMediaType(String mimeSubtype, String... kv) {
        MediaType tmp = new MediaType("application", mimeSubtype, parameters(kv));
        return tmp;
    }

    public static Map<String, String> parameters(String... kv) {
        if (kv == null || kv.length == 0) {
            return Collections.emptyMap();
        }

        if (kv.length / 2 == 0) {
            throw new IllegalArgumentException("key/value 数量错误");
        }


        HashMap<String, String> map = new HashMap<String, String>(kv.length / 2);
        for (int i = 0; i < kv.length / 2; i += 2) {
            map.put(kv[i], kv[i + 1]);
        }
        return map;
    }
}
