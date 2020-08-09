package cn.procsl.ping.boot.domain.business.service;

import cn.procsl.ping.boot.domain.business.entity.AdjacencyNode;
import cn.procsl.ping.boot.domain.support.business.AdjacencyTreeRepository;

import javax.inject.Inject;

/**
 * 邻接树服务
 *
 * @author procsl
 * @date 2020/07/31
 */
//@Named
//@Singleton
public class AdjacencyTreeService<Entity extends AdjacencyNode, ID> {

    @Inject
    AdjacencyTreeRepository<Entity, ID> adjacencyRepository;

}
