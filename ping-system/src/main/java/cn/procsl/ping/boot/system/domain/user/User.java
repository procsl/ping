package cn.procsl.ping.boot.system.domain.user;


import cn.procsl.ping.boot.jpa.RepositoryCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "s_user")
@NoArgsConstructor
@RepositoryCreator
public class User implements Serializable {

    @Id
    @GeneratedValue
    Long id;

    @Schema(defaultValue = "用户名称")
    String name;

    @Schema(defaultValue = "用户性别")
    @Enumerated(EnumType.STRING)
    Gender gender;

    @Schema(defaultValue = "备注")
    String remark;

    @Schema(defaultValue = "用户账户ID")
    @OneToOne(cascade = CascadeType.PERSIST)
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
