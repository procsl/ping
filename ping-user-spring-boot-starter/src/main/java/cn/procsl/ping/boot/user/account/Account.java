package cn.procsl.ping.boot.user.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 用户账户
 */
@Getter
@Setter
@Entity
@Table(name = "u_account")
@NoArgsConstructor
@AllArgsConstructor
public class Account extends AbstractPersistable<Long> implements Serializable {

    Long userId;

    String name;

    String password;

    /**
     * enable, disable
     */
    String status;

    public Account(String name) {
        this.name = name;
    }

    public void enabled() {
        this.status = "enable";
    }

    public boolean isDisabled() {
        return status == null || "disabled".equals(status);
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }
}
