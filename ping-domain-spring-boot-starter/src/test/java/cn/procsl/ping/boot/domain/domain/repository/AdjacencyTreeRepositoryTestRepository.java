package cn.procsl.ping.boot.domain.domain.repository;

import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.domain.model.PathNode;
import cn.procsl.ping.boot.domain.domain.model.Tree;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface AdjacencyTreeRepositoryTestRepository extends AdjacencyTreeRepository<Tree, Long, PathNode> {


    @Query("select tree from Tree as tree where tree.id in (select b.pathId from Tree as a inner join a.path as b where b.id =:id) order by tree.depth asc")
    Collection<Tree> parentTrees();

}
