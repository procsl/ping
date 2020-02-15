package cn.procsl.ping.web.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @author procsl
 * @date 2020/02/14
 */
@Controller
@RequestMapping("date")
public class DateHttpMessageConverterController {

    @GetMapping("general-date")
    public Date dateType() {
        return new Date();
    }
}
