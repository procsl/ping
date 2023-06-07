package cn.procsl.ping.boot.captcha.handler;

import cn.procsl.ping.boot.captcha.domain.VerifyCaptcha;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;

@RequiredArgsConstructor
class SimpleVerifyCommand implements VerifyCaptchaCommand {

    final protected HttpServletRequest request;

    public static String getTarget(HttpServletRequest request) {
        String sessionId = request.getRequestedSessionId();
        if (sessionId == null) {
            sessionId = request.getSession().getId();
        }
        return sessionId;
    }

    @Override
    public String target() {
        return getTarget(request);
    }

    @Override
    public String ticket() {
        String ticket = request.getHeader(VerifyCaptcha.header);

        if (ObjectUtils.isEmpty(ticket)) {
            throw new CaptchaNotFoundException("请输入邮件验证码");
        }

        return ticket;
    }

}
