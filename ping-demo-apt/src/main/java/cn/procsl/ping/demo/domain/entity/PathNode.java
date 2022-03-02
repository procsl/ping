package cn.procsl.ping.demo.domain.entity;

import cn.procsl.ping.boot.domain.base.tree.model.AdjacencyPathNode;

import javax.persistence.Embeddable;

@Embeddable
public class PathNode implements AdjacencyPathNode<String> {

    /**
     * 路径树 父节点ID
     *
     * @return pathId
     */
    @Override
    public String getPathId() {
        return null;
    }

    public void setPathId(String pathId) {
    }

    /**
     * 当前节点相对于root节点id的序号, root节点为seq为0
     *
     * @return seq
     */
    @Override
    public Integer getSeq() {
        return null;
    }
}
