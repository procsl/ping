package cn.procsl.ping.boot.admin.domain.user;


import cn.procsl.ping.boot.common.jpa.RepositoryCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "i_user")
@NoArgsConstructor
@RepositoryCreator
public class User extends AbstractPersistable<Long> implements Serializable {

    @Schema(description = "用户名称")
    String name;

    @Schema(description = "用户性别")
    @Enumerated(EnumType.STRING)
    Gender gender;

    @Schema(description = "身份证号")
    String cardNumber;

    @Schema(description = "民族")
    String nation;

    @Schema(description = "生日")
    Date birthday;

    @Schema(description = "毕业院校")
    String graduateSchool;

    @Schema(description = "电话")
    String telephone;

    @Schema(description = "邮箱")
    String email;

    @Schema(description = "备注")
    String remark;

    @Schema(description = "用户账户ID")
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

    public void updateSelf(String name, Gender gender, String cardNumber, String nation, Date birthday,
                           String graduateSchool, String telephone, String email, String remark) {
        this.name = name;
        this.gender = gender;
        this.remark = remark;
        this.birthday = birthday;
        this.graduateSchool = graduateSchool;
        this.email = email;
        this.nation = nation;
        this.telephone = telephone;
        this.cardNumber = cardNumber;
    }

}
