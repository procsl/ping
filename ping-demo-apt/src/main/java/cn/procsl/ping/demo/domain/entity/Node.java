package cn.procsl.ping.demo.domain.entity;

import cn.procsl.ping.boot.domain.jpa.tree.AdjacencyNode;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table
//@RepositoryCreator
public class Node extends User<PathNode> {

    @Id
    String id;

    @Override
    public String getId() {
        return null;
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
    public Set<PathNode> getPath() {
        return null;
    }

    /**
     * 创建路径节点实例方法
     *
     * @return 返回当前节点的 DictPath
     */
    @Override
    public PathNode currentPathNode() {
        return null;
    }


    /**
     * 修改父节点
     *
     * @param parent 指定的父节点
     */
    @Override
    public void changeParent(@NonNull AdjacencyNode<String, PathNode> parent) {

    }
}
