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


    @Description(comment = "父节点的ID")
    @Column(nullable = false, length = 32)
    protected String parentId;

    @Override
    public TreeNode<String> create(@NonNull String s) {
        TreeNode<String> root = this.getRoot();
        root.setParentId(s);
        root.setDepth(0);
        root.setPath(this.buildPath(this.getRootPath(), s));
        return root;
    }

    @Override
    public TreeNode<String> getRoot() {
        TreeNodeTest root = new TreeNodeTest();
        root.setDepth(0);
        root.setPath(this.getRootPath());
        root.setParentId("root");
        return root;
    }
}
