package cn.procsl.ping.boot.data.business.entity;

import cn.procsl.ping.boot.data.annotation.DefaultValue;
import cn.procsl.ping.boot.data.annotation.Description;
import lombok.Data;
import lombok.NonNull;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * 树节点结构
 *
 * @author procsl
 * @date 2020/05/09
 */
@Data
@MappedSuperclass
public abstract class TreeNode<ID> implements Serializable {

    @Description(comment = "父节点的ID")
    @Column(nullable = false)
    protected ID parentId;

    @Description(comment = "ID路径, 以/分割, 默认为/自身ID")
    @Column(length = 1000, nullable = false)
    protected String path;

    @Description(comment = "深度, 默认深度为0")
    @DefaultValue("0")
    @Column(nullable = false)
    protected Integer depth;

    /**
     * 创建层级为1的节点, 通过ID
     *
     * @param id 节点ID
     * @return 返回自身节点
     */
    public abstract TreeNode<ID> create(@NonNull ID id);

    /**
     * 获取根节点
     *
     * @return
     */
    @Transient
    public abstract TreeNode<ID> getRoot();

    protected String buildPath(@NonNull TreeNode<ID> node, @NonNull ID id) {
        if (StringUtils.isEmpty(node.getPath())) {
            return this.getDelimiter() + id.toString();
        }
        String tmp = node.getPath();
        if (ObjectUtils.nullSafeEquals(tmp.charAt(tmp.length() - 1), this.getDelimiter())) {
            return node.getPath() + id;
        }
        return node.getPath() + this.getDelimiter() + id;
    }

    protected String buildPath(String parentPath, @NonNull ID id) {
        if (StringUtils.isEmpty(parentPath)) {
            return this.getDelimiter() + id.toString();
        }

        if (ObjectUtils.nullSafeEquals(parentPath.charAt(parentPath.length() - 1), this.getDelimiter())) {
            return parentPath + id;
        }
        return parentPath + this.getDelimiter() + id;
    }

    @Transient
    @NonNull
    public char getDelimiter() {
        return '/';
    }

    /**
     * 获取
     *
     * @return
     */
    @Transient
    public String getRootPath() {
        return String.valueOf(this.getDelimiter());
    }
}
