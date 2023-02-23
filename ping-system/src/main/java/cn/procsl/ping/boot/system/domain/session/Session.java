package cn.procsl.ping.boot.system.domain.session;

import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户会话信息表
 */
@Getter
@Setter
@Entity
@RepositoryCreator(repositoryName = "pingAdminSessionRepository")
@Table(name = "i_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Session implements Serializable {

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

    SessionState state;

    @Builder
    public static Session createSession(String sessionId, String account, String username, Long userId) {
        Session session = new Session();
        session.setSessionId(sessionId);
        session.setNickName(username);
        session.setAccount(account);
        session.setUserId(userId);
        session.setState(SessionState.online);
        session.setLoginDate(new Date());
        session.setLogoutDate(null);
        return session;
    }

    public void logout() {
        if (this.state == SessionState.online) {
            this.state = SessionState.offline;
            this.logoutDate = new Date();
        }
    }

}
