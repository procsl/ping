package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.data.annotation.Description;
import cn.procsl.ping.boot.data.business.entity.TreeNode;
import cn.procsl.ping.boot.data.business.entity.GeneralEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

import static javax.persistence.EnumType.STRING;

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

    protected final static String RESOURCE_ID_NAME = "resource_id";

    @Id
    @Column(length = GENERAL_ENTITY_ID_LENGTH, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Description(comment = "资源名称")
    @Column(length = 20)
    protected String name;

    @Embedded
    @Basic(fetch = FetchType.LAZY)
    protected TreeNode<Long> node;

    @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {RESOURCE_ID_NAME, "operation"}))
    @ElementCollection
    @Column(name = "operation", updatable = false, length = 20)
    @Description(comment = "支持的操作, 针对当前关联的资源")
    protected Set<String> operations;

    @Description(comment = "资源类型")
    @Enumerated(STRING)
    protected ResourceType type;
}
