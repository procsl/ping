package cn.procsl.ping.boot.captcha.handler;

import jakarta.servlet.http.HttpServletRequest;

public final class WebUtils {

    public static String getClientSessionId(HttpServletRequest request) {
        String sessionId = request.getRequestedSessionId();
        if (sessionId == null) {
            sessionId = request.getSession().getId();
        }
        return sessionId;
    }

}
