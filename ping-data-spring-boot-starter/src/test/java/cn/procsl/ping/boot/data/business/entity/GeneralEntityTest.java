package cn.procsl.ping.boot.data.business.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

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
    @GeneratedValue(generator = "generator")
    @GenericGenerator(name = "generator", strategy = "uuid")
    String id;

    @Column(length = 20, nullable = false)
    String name;

    @Embedded
    TreeNodeTest nodeTest;

}
