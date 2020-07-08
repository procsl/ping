package cn.procsl.ping.boot.user.domain.res.entity;

import cn.procsl.ping.boot.data.annotation.Description;
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
@EqualsAndHashCode(callSuper = true)
public class ResourceTreeNode extends TreeNode<Long> {

    @Description(comment = "父节点的ID")
    @Column(nullable = false, length = GENERAL_ENTITY_ID_LENGTH)
    protected Long parentId;

    @Override
    public ResourceTreeNode create(Long parentId) {
        if (isRoot(parentId)) {
            return getRoot();
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

    public static boolean isRoot(Long parentId) {
        return parentId == null || parentId < 0;
    }

    public static boolean isRoot(ResourceTreeNode current) {
        return current == null || isRoot(current.getParentId());
    }


    public final static ResourceTreeNode root = new ResourceTreeNode() {
        {
            this.parentId = this.getParentId();
            this.depth = this.getDepth();
            this.path = this.getPath();
        }

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

}
