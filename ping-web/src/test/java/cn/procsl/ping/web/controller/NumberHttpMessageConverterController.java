package cn.procsl.ping.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author procsl
 * @date 2020/02/14
 */
@Controller
@RequestMapping("number")
public class NumberHttpMessageConverterController {

    @GetMapping("long")
    public Long longType(){
        return 10L;
    }

    @GetMapping("integer")
    public Integer IntegerType(){
        return 10;
    }

    @GetMapping("short")
    public Short shortType(){
        return 10;
    }

    @GetMapping("double")
    public Double doubleType(){
        return 10D;
    }


    @GetMapping("float")
    public Float floatType(){
        return 10F;
    }

    @GetMapping("byte")
    public byte byteType(){
        return 1;
    }
}
