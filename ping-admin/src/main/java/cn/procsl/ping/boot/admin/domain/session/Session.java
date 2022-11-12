package cn.procsl.ping.boot.admin.domain.session;

import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
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
    String username;

    @Column(length = 32)
    String sessionId;

    Date loginDate;

    @Column(length = 30)
    String ip;

    SessionState state;

}
