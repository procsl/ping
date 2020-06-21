package cn.procsl.ping.boot.user.domain.rbac.model;

import cn.procsl.ping.boot.data.business.entity.TreeNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import static cn.procsl.ping.boot.data.business.entity.GeneralEntity.GENERAL_ENTITY_ID_LENGTH;

/**
 * @author procsl
 * @date 2020/05/10
 */
@Embeddable
@Setter
@Getter
public class ResourceTreeNode extends TreeNode<Long> {

    private final static ResourceTreeNode root = new ResourceTreeNode() {

        @Override
        public final String getPath() {
            return getRootPath();
        }

        @Override
        public final Integer getDepth() {
            return 0;
        }

        @Override
        public final Long getParentId() {
            return -1L;
        }
    };

    @Column(nullable = false, length = GENERAL_ENTITY_ID_LENGTH)
    protected Long parentId;

    @Override
    public ResourceTreeNode create(@NonNull Long id) {
        ResourceTreeNode tmp = new ResourceTreeNode();
        tmp.setParentId(id);
        tmp.setDepth(1);
        tmp.setPath(this.buildPath(root, id));
        return tmp;
    }

    @Override
    @Transient
    public ResourceTreeNode getRoot() {
        return root;
    }
}
