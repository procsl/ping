package cn.procsl.ping.boot.user;


import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "u_user")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends AbstractPersistable<Long> implements User {

    String name;

    Gender gender;

    String remark;

}
