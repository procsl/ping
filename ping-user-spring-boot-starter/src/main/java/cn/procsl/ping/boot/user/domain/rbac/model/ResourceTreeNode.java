package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.data.business.entity.TreeNode;
import lombok.Data;
import lombok.NonNull;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import static cn.procsl.ping.boot.data.business.entity.GeneralEntity.GENERAL_ENTITY_ID_LENGTH;

/**
 * @author procsl
 * @date 2020/05/10
 */
@Embeddable
@Data
public class ResourceTreeNode extends TreeNode<Long> {

    @Column(nullable = false, length = GENERAL_ENTITY_ID_LENGTH)
    protected Long parentId;

    @Override
    public ResourceTreeNode create(@NonNull Long id) {
        return null;
    }

    @Override
    @Transient
    public TreeNode<Long> getRoot() {
        return null;
    }
}
