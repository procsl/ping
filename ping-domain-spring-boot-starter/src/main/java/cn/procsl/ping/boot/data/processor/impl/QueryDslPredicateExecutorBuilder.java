package cn.procsl.ping.boot.data.processor.impl;

import cn.procsl.ping.boot.data.processor.EntityRepositoryBuilder;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * @author procsl
 * @date 2020/06/21
 */
public class QueryDslPredicateExecutorBuilder extends EntityRepositoryBuilder {

    @Override
    protected Class getSupportRepositoryClass() {
        return QuerydslPredicateExecutor.class;
    }
}
