package cn.procsl.ping.boot.user.domain.res.entity;

import cn.procsl.ping.boot.data.annotation.CreateRepository;
import cn.procsl.ping.boot.data.annotation.Description;
import cn.procsl.ping.boot.data.business.entity.GeneralEntity;
import cn.procsl.ping.boot.user.utils.CollectionUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.Set;

import static javax.persistence.EnumType.STRING;
import static lombok.AccessLevel.PRIVATE;

/**
 * @author procsl
 * @date 2020/05/09
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
@Getter
@NoArgsConstructor
@Setter(PRIVATE)
@Description(comment = "资源聚合")
@CreateRepository
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
    protected ResourceTreeNode node;

    @CollectionTable(uniqueConstraints = @UniqueConstraint(columnNames = {RESOURCE_ID_NAME, "depend_id"}))
    @ElementCollection
    @Column(name = "depend_id", nullable = false, updatable = false, length = GENERAL_ENTITY_ID_LENGTH)
    @Description(comment = "依赖的资源")
    protected Set<Long> depends;

    @Description(comment = "资源类型")
    @Enumerated(STRING)
    @Column(length = 15)
    protected ResourceType type;

    public void rename(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("资源名称不可为空");
        }

        if (name.length() > 20) {
            throw new IllegalArgumentException("资源名称过长");
        }
        this.name = name;
    }

    public void grantDepend(Long resourceId) {
        this.depends = CollectionUtils.createAndAppend(this.depends, resourceId);
    }

    public void revokeDepend(Long resourceId) {
        CollectionUtils.nullSafeRemove(this.depends, resourceId);
    }

    public void changeType(ResourceType type) {
        if (type == null) {
            throw new IllegalArgumentException("资源类型不可为null");
        }

        this.type = type;
    }

    public void changeParentNode(Long parentId) {
        ResourceTreeNode parent = ResourceTreeNode.root.create(parentId);
        this.node = parent;
    }

    @Builder(buildMethodName = "done", builderMethodName = "creator")
    public Resource(String name, Long parentId, Set<Long> depends, ResourceType type) {
        this.rename(name);
        this.changeType(type);
        this.changeParentNode(parentId);
        this.depends = depends;
    }


}
