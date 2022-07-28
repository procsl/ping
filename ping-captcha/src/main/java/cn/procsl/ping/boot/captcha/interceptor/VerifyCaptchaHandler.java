package cn.procsl.ping.boot.captcha.interceptor;

import cn.procsl.ping.boot.captcha.web.VerifyCaptcha;
import cn.procsl.ping.boot.common.error.BusinessException;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VerifyCaptchaHandler implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler)
            throws Exception {
        boolean isHandler = handler instanceof HandlerMethod;
        if (!isHandler) {
            return true;
        }
        VerifyCaptcha markup = ((HandlerMethod) handler).getMethodAnnotation(VerifyCaptcha.class);
        if (markup == null) {
            return true;
        }
        String token = request.getHeader(VerifyCaptcha.header);
        if (token == null || token.isBlank()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "003", "请校验%s验证码", markup.type().getMessage());
        }
        // TODO 校验验证码
        return true;
    }
}
