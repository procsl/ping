package cn.procsl.ping.processor.repo.builder;

import cn.procsl.ping.processor.repo.EntityRepositoryBuilder;


/**
 * @author procsl
 * @date 2020/06/21
 */
//@AutoService(RepositoryBuilder.class)
public class QueryDslPredicateExecutorBuilder extends EntityRepositoryBuilder {

    @Override
    protected String getSupportRepositoryClass() {
        return "org.springframework.data.querydsl.QuerydslPredicateExecutor";
    }
}
