package cn.procsl.ping.demo.domain.entity;

import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode;

import javax.persistence.Embeddable;

@Embeddable
public class PathNode implements AdjacencyPathNode<String> {

    /**
     * 路径树 父节点ID
     *
     * @return
     */
    @Override
    public String getPathId() {
        return null;
    }

    /**
     * 当前节点相对于root节点id的序号, root节点为seq为0
     *
     * @return
     */
    @Override
    public Integer getSeq() {
        return null;
    }
}
