package cn.procsl.ping.boot.captcha.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.NonNull;
import org.springframework.util.ObjectUtils;

import static cn.procsl.ping.boot.captcha.domain.image.ImageCaptcha.TOKEN_KEY;

class ImageVerifyCommand extends SimpleVerifyCommand {

    public ImageVerifyCommand(HttpServletRequest request) {
        super(request);
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

}
