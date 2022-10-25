package cn.procsl.ping.boot.captcha.web;

import cn.procsl.ping.boot.captcha.domain.CaptchaType;
import cn.procsl.ping.boot.captcha.domain.image.ImageCaptcha;
import cn.procsl.ping.boot.captcha.domain.image.ImageCaptchaBuilderService;
import cn.procsl.ping.boot.captcha.domain.VerifyCaptcha;
import cn.procsl.ping.boot.captcha.handler.EmailCaptchaHandler;
import cn.procsl.ping.boot.common.web.Accepted;
import cn.procsl.ping.boot.common.web.Created;
import com.wf.captcha.ChineseCaptcha;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import static cn.procsl.ping.boot.captcha.domain.image.ImageCaptcha.token_key;

@RestController
@RequiredArgsConstructor
@Tag(name = "captcha", description = "验证码")
public class CaptchaController {

    final ImageCaptchaBuilderService imageCaptchaBuilderService = new ImageCaptchaBuilderService();

    final EmailCaptchaHandler emailCaptchaHandler;

    final AtomicLong auto = new AtomicLong(0);


    // TODO  应该修改为只要成功过一次, 就必须更换验证码, 如果校验失败, 则最多3次
    @PermitAll
    @Created(path = "/v1/captcha/image", produces = MediaType.IMAGE_GIF_VALUE, summary = "创建图形验证码")
    public void createImageCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sessionId = getTarget(request);

        ChineseCaptcha captcha = new ChineseCaptcha();
        long id = System.currentTimeMillis() + auto.getAndIncrement();
        ImageCaptcha imageCaptcha = new ImageCaptcha(id, sessionId, captcha.text(), 2);
        String token = this.imageCaptchaBuilderService.buildToken("123456", imageCaptcha);

        Cookie cookie = new Cookie(token_key, token);
        cookie.setMaxAge(imageCaptcha.validSecond());
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setContentType("image/gif");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        captcha.out(response.getOutputStream());
    }


    public static String getTarget(HttpServletRequest request) {
        String sessionId = request.getRequestedSessionId();
        if (sessionId == null) {
            sessionId = request.getSession().getId();
        }
        return sessionId;
    }

    @PermitAll
    @VerifyCaptcha(type = CaptchaType.image)
    @Accepted(path = "/v1/captcha/email", summary = "发送邮件验证码")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void sendEmailCaptcha(HttpServletRequest request, @RequestBody @Validated EmailSenderDTO sender) {
        this.emailCaptchaHandler.createEmailCaptcha(getTarget(request), sender.getEmail());
    }

}
