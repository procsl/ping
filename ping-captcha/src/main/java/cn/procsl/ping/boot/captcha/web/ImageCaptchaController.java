package cn.procsl.ping.boot.captcha.web;

import cn.procsl.ping.boot.captcha.domain.image.ImageCaptchaService;
import cn.procsl.ping.boot.common.web.Created;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Tag(name = "captcha", description = "图形验证码")
public class ImageCaptchaController {

    final ImageCaptchaService generatedCaptchaService;

    @PermitAll
    @Created(path = "/v1/captcha/image", produces = MediaType.IMAGE_GIF_VALUE, summary = "创建图形验证码")
    public void imageCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sessionId = request.getRequestedSessionId();
        if (sessionId == null) {
            sessionId = request.getSession().getId();
        }
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
//        this.generatedCaptchaService.generated(sessionId, CaptchaType.image, response.getOutputStream());
    }

}
