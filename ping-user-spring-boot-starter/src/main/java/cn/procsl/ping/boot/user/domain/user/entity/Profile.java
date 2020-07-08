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
 * 用户配置
 *
 * @author procsl
 * @date 2020/07/07
 */
@Entity
@Table
@Getter
@NoArgsConstructor
@Setter(PRIVATE)
@Description(comment = "用户配置")
@CreateRepository
public class Profile implements Serializable {

    @Id
    protected Long id;

}
