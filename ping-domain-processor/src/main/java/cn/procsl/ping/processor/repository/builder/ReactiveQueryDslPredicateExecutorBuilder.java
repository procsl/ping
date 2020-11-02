package cn.procsl.ping.processor.repository.builder;

import cn.procsl.ping.processor.repository.EntityRepositoryBuilder;
import cn.procsl.ping.processor.repository.RepositoryBuilder;
import com.google.auto.service.AutoService;

/**
 * @author procsl
 * @date 2020/06/21
 */
@AutoService(RepositoryBuilder.class)
public class ReactiveQueryDslPredicateExecutorBuilder extends EntityRepositoryBuilder {

    @Override
    protected String getSupportRepositoryClass() {
        return "org.springframework.data.querydsl.ReactiveQuerydslPredicateExecutor";
    }

    @Override
    public String getName() {
        return "ReactiveQueryDslPredicate";
    }

    /**
     * 这个接口有冲突, 需要独立
     *
     * @return true
     */
    @Override
    public boolean isSingleton() {
        return true;
    }
}
