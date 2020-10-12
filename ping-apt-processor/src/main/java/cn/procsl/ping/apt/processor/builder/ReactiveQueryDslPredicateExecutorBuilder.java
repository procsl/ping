package cn.procsl.ping.apt.processor.builder;

import cn.procsl.ping.apt.processor.EntityRepositoryBuilder;

/**
 * @author procsl
 * @date 2020/06/21
 */
public class ReactiveQueryDslPredicateExecutorBuilder extends EntityRepositoryBuilder {
    @Override
    protected Class<?> getSupportRepositoryClass() throws ClassNotFoundException {
        return Class.forName("org.springframework.data.querydsl.ReactiveQuerydslPredicateExecutor");
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
