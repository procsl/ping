package cn.procsl.ping.boot.data.business.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * 测试实体
 *
 * @author procsl
 * @date 2020/05/11
 */
@Entity
@Setter
@Getter
@Table
public class GeneralEntityTest extends GeneralEntity {


    @Id
    @Column(length = 32, updatable = false)
    String id;


    @Embedded
    TreeNodeTest nodeTest;

}
