package cn.procsl.ping.boot.domain.business.dictionary.repository;

import cn.procsl.ping.boot.domain.business.dictionary.model.DictPath;
import cn.procsl.ping.boot.domain.business.dictionary.model.Dictionary;
import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Repository;

@Repository
@Indexed
public interface DictionaryRepository extends
        AdjacencyTreeRepository<Dictionary, Long, DictPath>,
        JpaRepository<Dictionary, Long>,
        QuerydslPredicateExecutor<Dictionary> {

}
