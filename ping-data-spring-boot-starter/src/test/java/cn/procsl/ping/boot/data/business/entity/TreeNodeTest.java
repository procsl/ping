package cn.procsl.ping.boot.data.business.entity;

import cn.procsl.ping.boot.data.annotation.Description;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author procsl
 * @date 2020/05/11
 */
@Setter
@Getter
@Embeddable
public class TreeNodeTest extends TreeNode<String> {

    public static final TreeNodeTest root = new TreeNodeTest() {
        @Override
        public TreeNodeTest getRoot() {
            return this;
        }

        @Override
        public Integer getDepth() {
            return 0;
        }

        @Override
        public String getPath() {
            return this.getRootPath();
        }

        @Override
        public String getParentId() {
            return "root";
        }
    };

    @Description(comment = "父节点的ID")
    @Column(length = 32)
    protected String parentId;

    @Override
    public TreeNodeTest create(@NonNull String s) {
        TreeNodeTest tmp = this.getRoot();
        tmp.setParentId(s);
        tmp.setDepth(0);
        tmp.setPath(this.buildPath(this.getRootPath(), s));
        return root;
    }

    @Override
    public TreeNodeTest getRoot() {
        return root;
    }
}
