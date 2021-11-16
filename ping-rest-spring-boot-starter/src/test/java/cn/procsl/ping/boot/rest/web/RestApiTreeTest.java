package cn.procsl.ping.boot.rest.web;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

/**
 * @author procsl
 * @date 2020/02/22
 */
@Slf4j
public class RestApiTreeTest {

    @Test
    public void test() {
        MediaType mime = new MediaType("application", "api.ping+xml", StandardCharsets.UTF_8);
        log.info(mime.toString());
    }

}
