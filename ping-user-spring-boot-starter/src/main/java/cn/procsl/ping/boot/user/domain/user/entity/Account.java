package cn.procsl.ping.boot.user.domain.user.entity;

import cn.procsl.ping.boot.data.annotation.CreateRepository;
import cn.procsl.ping.boot.data.annotation.Description;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

import static lombok.AccessLevel.PRIVATE;

/**
 * 用户账户
 *
 * @author procsl
 * @date 2020/07/07
 */
@Entity
@Table
@Getter
@NoArgsConstructor
@Setter(PRIVATE)
@Description(comment = "用户账户")
@CreateRepository
public class Account implements Serializable {

    @Id
    private Long id;


}
