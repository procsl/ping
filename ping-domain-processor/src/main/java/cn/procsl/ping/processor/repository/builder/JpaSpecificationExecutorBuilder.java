package cn.procsl.ping.processor.repository.builder;

import cn.procsl.ping.processor.repository.processor.EntityRepositoryBuilder;
import cn.procsl.ping.processor.repository.processor.RepositoryBuilder;
import com.google.auto.service.AutoService;


/**
 * @author procsl
 * @date 2020/06/21
 */
@AutoService(RepositoryBuilder.class)
public class JpaSpecificationExecutorBuilder extends EntityRepositoryBuilder {
    @Override
    protected String getSupportRepositoryClass() {
        return "org.springframework.data.jpa.repository.JpaSpecificationExecutor";
    }
}
