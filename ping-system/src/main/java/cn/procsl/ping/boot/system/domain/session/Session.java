package cn.procsl.ping.boot.system.domain.session;

import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

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
public class Session extends AbstractPersistable<Long> implements Serializable {

    Long userId;

    @Column(length = 50)
    String account;

    @Column(length = 50)
    String nickName;

    @Column(length = 32)
    String sessionId;

    Date loginDate;

    Date logoutDate;

    @Column(length = 30)
    String ip;

    SessionState state;

    @Builder
    public static Session createSession(String sessionId, String ip, String account, String username, Long userId) {
        Session session = new Session();
        session.setSessionId(sessionId);
        session.setIp(ip);
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
