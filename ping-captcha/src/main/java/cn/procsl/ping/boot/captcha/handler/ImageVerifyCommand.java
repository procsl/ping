package cn.procsl.ping.boot.captcha.handler;

import org.springframework.lang.NonNull;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import static cn.procsl.ping.boot.captcha.domain.image.ImageCaptcha.token_key;

class ImageVerifyCommand extends SimpleVerifyCommand {

    public ImageVerifyCommand(HttpServletRequest request) {
        super(request);
    }

    public String token() {
        Cookie[] cookies = this.request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (ObjectUtils.nullSafeEquals(cookie.getName(), token_key)) {
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
