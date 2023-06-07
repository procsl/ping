package cn.procsl.ping.app.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Indexed;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Indexed
@RestController
@RequestMapping
public class TestController {


    @GetMapping("/echo")
    public String echo(String input) {
        return input;
    }

}
