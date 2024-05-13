package cn.procsl.ping.boot.captcha.domain;

import java.util.Date;

public interface VerifyCaptchaCommand {

    /**
     * 客户端唯一标识
     */
    String getClientId();

    /**
     * 客户端令牌
     */
    String getClientTicket();

    /**
     * 对应的功能模块ID
     */
    String getFunctionId();

    /**
     * 用于校验的时间
     */
    default Date getVerifyDate() {
        return new Date();
    }

}
