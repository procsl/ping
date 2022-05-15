package cn.procsl.ping.boot.infra.user;


import cn.procsl.ping.processor.annotation.RepositoryCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "i_user")
@NoArgsConstructor
@RepositoryCreator
class User extends AbstractPersistable<Long> implements Serializable {

    @Schema(defaultValue = "用户名称")
    String name;

    @Schema(defaultValue = "用户性别")
    @Enumerated(EnumType.STRING)
    Gender gender;

    @Schema(defaultValue = "备注")
    String remark;

    @Schema(defaultValue = "用户账户ID")
    Long accountId;

    {
        if (gender == null) {
            this.gender = Gender.unknown;
        }
    }

    @Builder
    public User(String name, Long accountId) {
        this.name = name;
        this.accountId = accountId;
        this.gender = Gender.unknown;
    }
}
