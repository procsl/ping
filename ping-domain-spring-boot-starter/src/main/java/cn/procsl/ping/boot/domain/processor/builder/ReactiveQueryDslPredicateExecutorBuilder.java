package cn.procsl.ping.boot.domain.processor.builder;

import cn.procsl.ping.boot.domain.processor.EntityRepositoryBuilder;
import org.springframework.data.querydsl.ReactiveQuerydslPredicateExecutor;

/**
 * @author procsl
 * @date 2020/06/21
 */
public class ReactiveQueryDslPredicateExecutorBuilder extends EntityRepositoryBuilder {
    @Override
    protected Class<ReactiveQuerydslPredicateExecutor> getSupportRepositoryClass() {
        return ReactiveQuerydslPredicateExecutor.class;
    }
}
