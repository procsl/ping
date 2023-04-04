package cn.procsl.ping.app.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Indexed;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Indexed
@RestController
@Slf4j
public class ForwardController {

//    RestTemplate restTemplate = new RestTemplate();
//
//    @GetMapping("/baidu")
//    public void test() {
//        String string = restTemplate.getForObject("https://www.baidu.com/", String.class);
//        log.info("百度返回数据:{}", string);
//    }

}
