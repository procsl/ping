package cn.procsl.ping.boot.captcha.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@Tag(name = "captcha", description = "图形验证码")
public class ImageCaptchaController {

    @PermitAll
    @Operation(summary = "创建图形验证码")
    @PostMapping(value = "/v1/captcha/image", produces = MediaType.IMAGE_JPEG_VALUE)
    public void imageCaptcha(HttpServletRequest request, HttpServletResponse response) {

    }

}
