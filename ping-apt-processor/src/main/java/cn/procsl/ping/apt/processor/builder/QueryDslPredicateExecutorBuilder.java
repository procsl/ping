package cn.procsl.ping.apt.processor.builder;

import cn.procsl.ping.apt.processor.EntityRepositoryBuilder;


/**
 * @author procsl
 * @date 2020/06/21
 */
public class QueryDslPredicateExecutorBuilder extends EntityRepositoryBuilder {

    @Override
    protected Class<?> getSupportRepositoryClass() throws ClassNotFoundException {
        return Class.forName("org.springframework.data.querydsl.QuerydslPredicateExecutor");
    }
}
