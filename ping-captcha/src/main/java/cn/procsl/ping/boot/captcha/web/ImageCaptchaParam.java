package cn.procsl.ping.boot.captcha.web;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class ImageCaptchaParam implements Serializable {

    Integer width;

    Integer height;

    @NotBlank
    String functionId;


}
