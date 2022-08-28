package cn.procsl.ping.boot.captcha.domain;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.*;

import static cn.procsl.ping.boot.captcha.domain.VerifyCaptcha.header;

/**
 * 验证码标注
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(name = header,
        schema = @Schema(implementation = String.class),
        description = "验证码Ticket,需编码为Base64",
        required = true, in =
        ParameterIn.HEADER)
public @interface VerifyCaptcha {

    String header = "X-Captcha-Ticket";

    CaptchaType type() default CaptchaType.image;


}
