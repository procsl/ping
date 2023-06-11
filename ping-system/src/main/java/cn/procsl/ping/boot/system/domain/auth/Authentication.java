package cn.procsl.ping.boot.system.domain.auth;

import cn.procsl.ping.boot.jpa.RepositoryCreator;
import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户会话信息表
 */
@Getter
@Setter
@Entity
@RepositoryCreator(repositoryName = "PingSystemAuthenticationRepository")
@Table(name = "s_authentication")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Authentication implements Serializable {

    @Id
    @GeneratedValue
    Long id;

    @Column(updatable = false)
    Long userId;

    @Column(length = 50, updatable = false)
    String account;

    @Column(length = 50, updatable = false)
    String nickName;

    @Column(length = 36, updatable = false)
    String sessionId;

    @Column(updatable = false)
    Date loginDate;

    Date logoutDate;

    AuthenticationState state;

    @Builder
    public static Authentication createSession(String sessionId, String account, String username, Long userId) {
        Authentication authentication = new Authentication();
        authentication.setSessionId(sessionId);
        authentication.setNickName(username);
        authentication.setAccount(account);
        authentication.setUserId(userId);
        authentication.setState(AuthenticationState.online);
        authentication.setLoginDate(new Date());
        authentication.setLogoutDate(null);
        return authentication;
    }

    public void logout() {
        if (this.state == AuthenticationState.online) {
            this.state = AuthenticationState.offline;
            this.logoutDate = new Date();
        }
    }

}
