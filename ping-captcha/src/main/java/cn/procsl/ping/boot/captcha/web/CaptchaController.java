package cn.procsl.ping.boot.captcha.web;

import cn.procsl.ping.boot.captcha.domain.CaptchaType;
import cn.procsl.ping.boot.captcha.domain.VerifyCaptcha;
import cn.procsl.ping.boot.captcha.domain.image.ImageCaptcha;
import cn.procsl.ping.boot.captcha.domain.image.ImageCaptchaBuilderService;
import cn.procsl.ping.boot.captcha.handler.EmailCaptchaHandler;
import cn.procsl.ping.boot.web.VersionControl;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    public static String getTarget(HttpServletRequest request) {
        String sessionId = request.getRequestedSessionId();
        if (sessionId == null) {
            sessionId = request.getSession().getId();
        }
        return sessionId;
    }

    @PermitAll
    @VersionControl
    @Operation(summary = "创建图形验证码")
    @PostMapping(path = "/v1/captcha/image", produces = MediaType.IMAGE_GIF_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void createImageCaptcha(HttpServletRequest request, HttpServletResponse response,
                                   @RequestParam(defaultValue = "130", required = false) Integer width,
                                   @RequestParam(defaultValue = "48", required = false) Integer height)
            throws IOException {

        Captcha captcha = new SpecCaptcha();

        long id = System.currentTimeMillis() + auto.getAndIncrement();

        String sessionId = getTarget(request);
        ImageCaptcha imageCaptcha = new ImageCaptcha(id, sessionId, captcha.text(), 2);
        String token = this.imageCaptchaBuilderService.buildToken("123456", imageCaptcha);

        Cookie cookie = new Cookie(token_key, token);
        cookie.setMaxAge(imageCaptcha.validSecond());
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType(MediaType.IMAGE_GIF_VALUE);

        // 由于直接输出响应, 会影响 HttpStatus 的设置, 因此应该提前设置 HttpStatus
        response.setStatus(HttpStatus.CREATED.value());

        captcha.out(response.getOutputStream());
    }

    @PermitAll
    @VerifyCaptcha(type = CaptchaType.image)
    @Operation(summary = "发送邮件验证码")
    @PostMapping(path = "/v1/captcha/email")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    @Transactional(rollbackFor = Exception.class)
    public void sendEmailCaptcha(HttpServletRequest request, @RequestBody @Validated EmailSenderDTO sender) {
        this.emailCaptchaHandler.createEmailCaptcha(getTarget(request), sender.getEmail());
    }

}