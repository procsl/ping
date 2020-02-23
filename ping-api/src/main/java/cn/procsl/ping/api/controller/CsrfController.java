package cn.procsl.ping.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author procsl
 * @date 2020/02/17
 */
@RequestMapping
@Controller
public class CsrfController {

    @GetMapping("csrf")
    public void csrf() {
    }

}
