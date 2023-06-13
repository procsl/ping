package cn.procsl.ping.boot.system.domain.user;

import lombok.NonNull;

public class AuthenticateService {

    public Authenticate doLogin(@NonNull String sessionId, @NonNull String password, User user) {
        if (user == null) {
            throw new AuthenticateException("用户名或密码错误");
        }
        user.getAccount().authenticate(password);
        return Authenticate.builder()
                      .userId(user.getId())
                      .account(user.getAccount().getName())
                      .username(user.getName())
                      .sessionId(sessionId)
                      .build();
    }

}
