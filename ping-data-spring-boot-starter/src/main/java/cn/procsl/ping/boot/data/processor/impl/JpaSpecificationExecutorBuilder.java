package cn.procsl.ping.boot.data.processor.impl;

import cn.procsl.ping.boot.data.processor.EntityRepositoryBuilder;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author procsl
 * @date 2020/06/21
 */
public class JpaSpecificationExecutorBuilder extends EntityRepositoryBuilder {
    @Override
    protected Class getSupportRepositoryClass() {
        return JpaSpecificationExecutor.class;
    }
}
