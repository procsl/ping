package cn.procsl.ping.boot.system.domain.session;

import cn.procsl.ping.boot.system.domain.user.AuthenticateException;
import cn.procsl.ping.boot.system.domain.user.User;
import lombok.NonNull;

public class UserLoginService {

    public Session doLogin(@NonNull String sessionId, @NonNull String password, User user) {
        if (user == null) {
            throw new AuthenticateException("用户名或密码错误");
        }
        user.getAccount().authenticate(password);
        return Session.builder()
                      .userId(user.getId())
                      .account(user.getAccount().getName())
                      .username(user.getName())
                      .sessionId(sessionId)
                      .build();
    }

}
