package cn.procsl.ping.boot.user.account;

import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractAuditable;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 用户账户
 */
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "u_account")
@NoArgsConstructor
@RepositoryCreator
public class Account extends AbstractAuditable<Long, Long> implements Serializable {

    Long userId;

    String name;

    String password;

}
