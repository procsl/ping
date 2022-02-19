package cn.procsl.ping.boot.user;


import cn.procsl.ping.processor.annotation.RepositoryCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "u_user")
@NoArgsConstructor
@AllArgsConstructor
@RepositoryCreator
public class User extends AbstractPersistable<Long> implements Serializable {

    String name;

    /**
     * 男, 女, 保密, 未设置的
     */
    String gender;

    String remark;

}
