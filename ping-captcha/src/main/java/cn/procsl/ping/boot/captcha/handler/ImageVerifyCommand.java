package cn.procsl.ping.boot.captcha.handler;

import cn.procsl.ping.boot.captcha.domain.VerifyCaptcha;
import cn.procsl.ping.boot.captcha.domain.VerifyCaptchaCommand;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.util.ObjectUtils;

import static cn.procsl.ping.boot.captcha.domain.image.ImageCaptcha.TOKEN_KEY;

@RequiredArgsConstructor
class ImageVerifyCommand implements VerifyCaptchaCommand {
    final protected HttpServletRequest request;

    @Override
    public String getClientId() {
        return WebUtils.getClientSessionId(request);
    }

    @Override
    public String getClientTicket() {
        return request.getHeader(VerifyCaptcha.header);
    }


    public String getClientTokenString() {
        Cookie[] cookies = this.request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (ObjectUtils.nullSafeEquals(cookie.getName(), TOKEN_KEY)) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @NonNull
    public String key() {
        return "123456";
    }

    @Override
    public String getFunctionId() {
        return request.getMethod() + ":" + request.getRequestURI();
    }

}
