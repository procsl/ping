package cn.procsl.ping.business.user.impl.entity;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * 权限表
 * @author procsl
 * @date 2020/03/21
 */
@Data
@Entity
@Table
public class Permission {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;


    @Version
    protected Long version;

}
