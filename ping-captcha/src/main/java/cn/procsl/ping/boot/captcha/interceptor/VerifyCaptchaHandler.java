package cn.procsl.ping.boot.captcha.interceptor;

import cn.procsl.ping.boot.captcha.domain.VerifyCaptcha;
import cn.procsl.ping.boot.captcha.service.VerifyCaptchaService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public class VerifyCaptchaHandler implements HandlerInterceptor {

    final VerifyCaptchaService verifyCaptchaService;

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
        String sessionId = request.getRequestedSessionId();
        this.verifyCaptchaService.verify(sessionId, token, markup);
        return true;
    }
}
