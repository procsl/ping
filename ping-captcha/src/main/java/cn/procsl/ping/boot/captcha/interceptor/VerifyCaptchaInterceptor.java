package cn.procsl.ping.boot.captcha.interceptor;

import cn.procsl.ping.boot.captcha.domain.VerifyCaptcha;
import cn.procsl.ping.boot.captcha.handler.VerifyCaptchaHandlerStrategy;
import cn.procsl.ping.boot.common.web.AbstractMethodAnnotationInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;

public class VerifyCaptchaInterceptor extends AbstractMethodAnnotationInterceptor<VerifyCaptcha> {

    final VerifyCaptchaHandlerStrategy verifyCaptchaService;

    public VerifyCaptchaInterceptor(VerifyCaptchaHandlerStrategy verifyCaptchaService) {
        super(VerifyCaptcha.class);
        this.verifyCaptchaService = verifyCaptchaService;
    }


    @Override
    protected boolean doPreHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler,
                                  VerifyCaptcha annotation) {

        if (annotation == null) {
            return true;
        }
        verifyCaptchaService.verifyForStrategy(request, response, handler, annotation);
        return true;
    }
}
