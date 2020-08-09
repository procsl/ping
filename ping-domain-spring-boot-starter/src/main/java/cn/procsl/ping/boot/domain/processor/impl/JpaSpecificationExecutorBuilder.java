package cn.procsl.ping.boot.domain.processor.impl;

import cn.procsl.ping.boot.domain.processor.EntityRepositoryBuilder;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author procsl
 * @date 2020/06/21
 */
public class JpaSpecificationExecutorBuilder extends EntityRepositoryBuilder {
    @Override
    protected Class<JpaSpecificationExecutor> getSupportRepositoryClass() {
        return JpaSpecificationExecutor.class;
    }
}
