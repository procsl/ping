package cn.procsl.ping.demo.gen.handle;

import cn.procsl.ping.web.http.annotation.Get;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static java.util.Collections.EMPTY_MAP;

/**
 * @author procsl
 * @date 2019/10/07
 */
@Slf4j
public class EchoHandle {

    @Get(path = "/echo")
    public String echo() {
        log.debug("执行中");
        return "hello world";
    }

    @Get(path = "/echo")
    public Map<String, String> echo(String input) {
        return EMPTY_MAP;
    }
}
