package cn.procsl.ping.boot.admin.domain.user;


import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "i_user")
@NoArgsConstructor
@RepositoryCreator
public class User extends AbstractPersistable<Long> implements Serializable {

    @Schema(defaultValue = "用户名称")
    String name;

    @Schema(defaultValue = "用户性别")
    @Enumerated(EnumType.STRING)
    Gender gender;

    @Schema(defaultValue = "备注")
    String remark;

    @Schema(defaultValue = "用户账户ID")
    @OneToOne
    Account account;


    public static User creator(String name, String account, String password) {
        User user = new User();
        user.name = name;
        user.gender = Gender.unknown;
        user.account = Account.create(account, password);
        return user;
    }

    public void updateSelf(String name, Gender gender, String remark) {
        this.name = name;
        this.gender = gender;
        this.remark = remark;
    }
}
