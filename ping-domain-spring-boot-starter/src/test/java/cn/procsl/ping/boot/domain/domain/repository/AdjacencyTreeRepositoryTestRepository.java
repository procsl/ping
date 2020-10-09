package cn.procsl.ping.boot.domain.domain.repository;

import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.domain.model.Path;
import cn.procsl.ping.boot.domain.domain.model.Tree;
import org.springframework.stereotype.Repository;

@Repository
public interface AdjacencyTreeRepositoryTestRepository extends AdjacencyTreeRepository<Tree, Long, Path> {

}
