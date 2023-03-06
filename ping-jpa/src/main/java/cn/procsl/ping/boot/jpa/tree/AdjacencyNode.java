package cn.procsl.ping.boot.jpa.tree;

import lombok.NonNull;
import org.springframework.util.ObjectUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * 邻接表
 *
 * @author procsl
 * @date 2020/07/29
 */
@MappedSuperclass
public interface AdjacencyNode<ID extends Serializable, T extends AdjacencyPathNode<ID>> {

    int ROOT_DEPTH = 0;

    @Id
    ID getId();

    ID getParentId();

    Integer getDepth();

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "id"))
    Set<T> getPath();

    /**
     * 判断是否为根节点
     *
     * @return 如果为root节点, 则返回true
     */
    @Transient
    default boolean isRoot() {
        return ObjectUtils.nullSafeEquals(getId(), getParentId()) || ObjectUtils.nullSafeEquals(getDepth(), ROOT_DEPTH);
    }

    /**
     * 创建路径节点实例方法
     *
     * @return 返回当前节点的 path
     */
    T currentPathNode();

    /**
     * 修改父节点
     *
     * @param parent 指定的父节点
     */
    void changeParent(@NonNull AdjacencyNode<ID, T> parent);

    /**
     * 查找分隔符
     *
     * @return 分隔符
     */
    default String findDelimiter() {
        return "/";
    }
}
