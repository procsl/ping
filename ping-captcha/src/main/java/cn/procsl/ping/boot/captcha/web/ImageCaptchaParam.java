package cn.procsl.ping.boot.captcha.web;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class ImageCaptchaParam implements Serializable {

    @NotNull(message = "宽度不能为空")
    @Min(value = 80, message = "验证码宽度不能小于 80")
    private Integer width = 130;

    @NotNull(message = "高度不能为空")
    @Max(200)
    @Min(value = 30, message = "验证码高度不能小于 30")
    private Integer height = 48;


    @NotBlank
    @Pattern(
        regexp = "^(GET|POST|PUT|DELETE|PATCH):(/[a-zA-Z0-9_\\-]{1,30})+$",
        message = "格式不正确。例如: POST:/v1/system/authentications"
    )
    String functionId;


}
