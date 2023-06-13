package cn.procsl.ping.boot.system.domain.user;

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
@RepositoryCreator(repositoryName = "PingAuthenticateRepository")
@Table(name = "s_authenticate")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Authenticate implements Serializable {

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

    AuthenticateState state;

    @Builder
    public static Authenticate create(String sessionId, String account, String username, Long userId) {
        Authenticate authenticate = new Authenticate();
        authenticate.setSessionId(sessionId);
        authenticate.setNickName(username);
        authenticate.setAccount(account);
        authenticate.setUserId(userId);
        authenticate.setState(AuthenticateState.online);
        authenticate.setLoginDate(new Date());
        authenticate.setLogoutDate(null);
        return authenticate;
    }

    public void offline() {
        if (this.state == AuthenticateState.online) {
            this.state = AuthenticateState.offline;
            this.logoutDate = new Date();
        }
    }

}
