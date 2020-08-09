package cn.procsl.ping.boot.domain.domain.repository;

import cn.procsl.ping.boot.domain.domain.entity.TreeEntity;
import cn.procsl.ping.boot.domain.support.business.AdjacencyTreeRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdjacencyTreeRepositoryTestRepository extends AdjacencyTreeRepository<TreeEntity, String> {

}
