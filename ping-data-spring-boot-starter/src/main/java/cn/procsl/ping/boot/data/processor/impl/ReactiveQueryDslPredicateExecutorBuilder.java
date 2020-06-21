package cn.procsl.ping.boot.data.processor.impl;

import cn.procsl.ping.boot.data.processor.EntityRepositoryBuilder;
import org.springframework.data.querydsl.ReactiveQuerydslPredicateExecutor;

/**
 * @author procsl
 * @date 2020/06/21
 */
public class ReactiveQueryDslPredicateExecutorBuilder extends EntityRepositoryBuilder {
    @Override
    protected Class getSupportRepositoryClass() {
        return ReactiveQuerydslPredicateExecutor.class;
    }
}
