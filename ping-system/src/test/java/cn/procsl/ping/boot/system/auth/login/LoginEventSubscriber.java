package cn.procsl.ping.boot.system.auth.login;

import cn.procsl.ping.boot.common.event.Subscriber;
import cn.procsl.ping.boot.common.event.SubscriberRegister;
import lombok.extern.slf4j.Slf4j;

import static cn.procsl.ping.boot.system.constant.EventPublisherConstant.USER_LOGIN;
import static cn.procsl.ping.boot.system.constant.EventPublisherConstant.USER_LOGOUT;

@Slf4j
@SubscriberRegister
public class LoginEventSubscriber {


    @Subscriber(name = USER_LOGIN)
    public void subLogin(String name) {
        log.info("监听到用户登录事件:{}", name);
    }

    @Subscriber(name = USER_LOGOUT)
    public void subLogout(String name) {
        log.info("监听到注销用户事件:{}", name);
    }

}
