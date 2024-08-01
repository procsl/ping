package cn.procsl.ping.boot.jpa.support.query;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

@QueryForProjection
public interface MainRepository extends Repository<MainEntity, Long> {

    @Query(queryRewriter = Rewriter.class)
    MainEntity findGenerated(String name);

}
