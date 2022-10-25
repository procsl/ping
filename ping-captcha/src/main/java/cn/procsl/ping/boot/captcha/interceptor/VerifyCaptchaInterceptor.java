package cn.procsl.ping.boot.captcha.interceptor;

import cn.procsl.ping.boot.captcha.domain.VerifyCaptcha;
import cn.procsl.ping.boot.captcha.handler.VerifyCaptchaHandlerStrategy;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class VerifyCaptchaInterceptor implements HandlerInterceptor {

    final VerifyCaptchaHandlerStrategy verifyCaptchaService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        boolean isHandler = handler instanceof HandlerMethod;
        if (!isHandler) {
            return true;
        }
        VerifyCaptcha verifyAnnotation = ((HandlerMethod) handler).getMethodAnnotation(VerifyCaptcha.class);
        if (verifyAnnotation == null) {
            return true;
        }
        verifyCaptchaService.verifyForStrategy(request, response, (HandlerMethod) handler, verifyAnnotation);
        return true;
    }
}
