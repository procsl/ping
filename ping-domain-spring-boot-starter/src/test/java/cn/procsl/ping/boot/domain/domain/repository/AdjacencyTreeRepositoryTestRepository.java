package cn.procsl.ping.boot.domain.domain.repository;

import cn.procsl.ping.boot.domain.domain.entity.TreeEntity;
import cn.procsl.ping.boot.domain.support.business.AdjacencyTreeRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface AdjacencyTreeRepositoryTestRepository extends AdjacencyTreeRepository<TreeEntity, String> {


    //    @Query("select a from TreeEntity as a inner join TreeEntity.path as b on a.id = b.pathId where b.seq > 10 order by b.seq asc")
    @Query("select a from TreeEntity as a")
    Collection<TreeEntity> parentTrees();

}
