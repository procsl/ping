package cn.procsl.ping.boot.domain.business.entity;

import cn.procsl.ping.boot.domain.business.utils.CollectionUtils;
import cn.procsl.ping.business.domain.DomainEntity;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 邻接表
 *
 * @author procsl
 * @date 2020/07/29
 */
@MappedSuperclass
public interface AdjacencyNode<ID extends Serializable, T extends AdjacencyPathNode<ID>> extends Serializable, DomainEntity {

    ID getId();

    ID getParentId();

    Integer getDepth();

    @ElementCollection
    @CollectionTable(joinColumns = @JoinColumn(name = "id"))
    Set<T> getPath();

    /**
     * 通过指定的父节点信息填充当前节点
     *
     * @param parentNode 指定的父节点
     * @return 返回创建后的节点
     */
    void fullByParent(@Nonnull AdjacencyNode<ID, T> parentNode);

    /**
     * 获取root节点
     *
     * @return 返回当前树对象的根节点
     */
    AdjacencyNode<ID, T> root();

    @Transient
    boolean isRoot();

    /**
     * 追加之指定的节点并返回新节点
     *
     * @return 返回追加成功的节点集合
     */
    default Set<T> createPathBy(ID parentId, Collection<T> path) {
        if (isRoot()) {
            return Collections.emptySet();
        }
        int seq = CollectionUtils.isEmpty(path) ? 0 : path.size();

        Set<T> newPaths = new HashSet<>(seq + 1);
        newPaths.addAll(path);
        newPaths.add(this.createPathNode(parentId, seq + 1));
        return newPaths;
    }

    /**
     * 创建路径节点实例方法
     *
     * @param parentId
     * @param seq
     * @return
     */
    T createPathNode(ID parentId, Integer seq);

}
