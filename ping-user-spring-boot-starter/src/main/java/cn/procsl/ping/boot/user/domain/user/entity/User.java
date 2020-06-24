package cn.procsl.ping.boot.user.domain.user.entity;

import cn.procsl.ping.boot.data.annotation.CreateRepository;
import cn.procsl.ping.boot.data.annotation.Description;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import static lombok.AccessLevel.PRIVATE;

/**
 * 用户实体
 *
 * @author procsl
 * @date 2020/06/24
 */
@Entity
@Table
@Getter
@NoArgsConstructor
@Setter(PRIVATE)
@Description(comment = "用户实体")
@CreateRepository
public class User {

    @Id
    private Long id;

    private String name;

    private String desc;

}
