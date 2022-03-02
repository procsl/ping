package cn.procsl.ping.demo.domain.entity;

import cn.procsl.ping.boot.domain.base.tree.model.AdjacencyNode;

import javax.persistence.Id;

/**
 * @author procsl
 * @date 2020/05/18
 */
public abstract class User<T extends PathNode> implements AdjacencyNode<String, T> {

    @Id
    String id;


}
