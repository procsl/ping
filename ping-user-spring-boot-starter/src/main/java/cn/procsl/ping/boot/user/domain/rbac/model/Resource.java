package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.data.annotation.Description;
import cn.procsl.ping.boot.user.domain.common.GeneralEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

/**
 * @author procsl
 * @date 2020/05/09
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
@Getter
@NoArgsConstructor
@Setter
@Description(comment = "资源聚合")
public class Resource extends GeneralEntity {

    private final static String RESOURCE_ID_NAME = "resource_id";

    @Id
    @Column(length = GENERAL_ENTITY_ID_LENGTH, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Description(comment = "资源名称")
    @Column(length = 20)
    private String name;

    @Embedded
    @Basic(fetch = FetchType.LAZY)
    private PathTree<Long> pathTree;

    @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {RESOURCE_ID_NAME, "operation"}))
    @ElementCollection
    @Column(name = "operation", updatable = false, length = GENERAL_ENTITY_ID_LENGTH)
    @Description(comment = "支持的操作, 针对当前关联的资源")
    protected Set<String> operations;
}
