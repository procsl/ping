package cn.procsl.ping.boot.domain.processor.impl;

import cn.procsl.ping.boot.domain.processor.EntityRepositoryBuilder;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * @author procsl
 * @date 2020/06/21
 */
public class QueryDslPredicateExecutorBuilder extends EntityRepositoryBuilder {

    @Override
    protected Class<QuerydslPredicateExecutor> getSupportRepositoryClass() {
        return QuerydslPredicateExecutor.class;
    }
}
