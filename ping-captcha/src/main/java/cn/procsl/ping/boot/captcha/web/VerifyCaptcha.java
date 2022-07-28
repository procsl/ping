package cn.procsl.ping.boot.captcha.web;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.Getter;

import java.lang.annotation.*;

import static cn.procsl.ping.boot.captcha.web.VerifyCaptcha.header;

/**
 * 验证码标注
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(name = header, description = "验证码Token", required = true, in = ParameterIn.HEADER)
public @interface VerifyCaptcha {

    String header = "X-Captcha-Token";

    CaptchaType type() default CaptchaType.image;

    enum CaptchaType {
        image("图形");

        @Getter
        final String message;

        CaptchaType(String message) {
            this.message = message;
        }
    }

}
