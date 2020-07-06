package cn.procsl.ping.boot.user.domain.rbac.entity;

import cn.procsl.ping.boot.data.business.entity.TreeNode;
import lombok.*;

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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceTreeNode extends TreeNode<Long> {

    public final static ResourceTreeNode root = new ResourceTreeNode() {

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
    public ResourceTreeNode create(@NonNull Long parentId) {
        if (parentId == null || parentId < 0) {
            return root;
        }
        ResourceTreeNode tmp = new ResourceTreeNode();
        tmp.setParentId(parentId);
        tmp.setDepth(1);
        tmp.setPath(this.buildPath(root, parentId));
        return tmp;
    }

    @Override
    @Transient
    public ResourceTreeNode getRoot() {
        return root;
    }
}
