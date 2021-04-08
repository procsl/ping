package cn.procsl.ping.graalvm.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EchoController {

    @GetMapping("echo")
    public String echo(@RequestParam String input) {
        return input;
    }


    @GetMapping
    public String hello() {
        return "Hello world";
    }

}
