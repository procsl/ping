package cn.procsl.ping.processor.repository.builder;

import cn.procsl.ping.processor.repository.EntityRepositoryBuilder;
import cn.procsl.ping.processor.repository.RepositoryBuilder;
import com.google.auto.service.AutoService;


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
