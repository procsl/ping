package cn.procsl.ping.boot.system.domain.user;


import cn.procsl.ping.boot.jpa.support.RepositoryCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "s_user")
@NoArgsConstructor
@RepositoryCreator(repositories = {JpaSpecificationExecutor.class, JpaRepository.class})
public class User implements Serializable {

    @Id
    @TableGenerator(name = "ping_sequence")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ping_sequence")
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
        if (name == null || name.isEmpty()) {
            user.name = account;
        } else {
            user.name = name;
        }
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
