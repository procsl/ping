package cn.procsl.ping.demo.domain.entity;

import cn.procsl.ping.boot.domain.annotation.CreateRepository;
import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;


@Entity
@Table
@CreateRepository
public class Tree implements AdjacencyNode<String, PathNode>, Serializable {

    @Id
    String id;

    @Transient
    Set<PathNode> pathNodes;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getParentId() {
        return null;
    }

    @Override
    public Integer getDepth() {
        return null;
    }

    @Override
    @ElementCollection
    public Set<PathNode> getPath() {
        return pathNodes;
    }

    public void setPathNode(Set<PathNode> pathNode) {
        this.pathNodes = pathNodes;
    }

    /**
     * 创建路径节点实例方法
     *
     * @param pathId
     * @param seq
     * @return
     */
    @Override
    public PathNode createPathNode(String pathId, Integer seq) {
        return null;
    }

    /**
     * 修改父节点
     *
     * @param parent 指定的父节点
     */
    @Override
    public void changeParent(AdjacencyNode<String, PathNode> parent) {

    }
}
