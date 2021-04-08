package cn.procsl.ping.processor.repository.builder;

import cn.procsl.ping.processor.repository.EntityAndIdRepositoryBuilder;
import cn.procsl.ping.processor.repository.RepositoryBuilder;
import com.google.auto.service.AutoService;

/**
 * @author procsl
 * @date 2020/06/21
 */
@AutoService(RepositoryBuilder.class)
public class CrudRepositoryBuilder extends EntityAndIdRepositoryBuilder {
    @Override
    protected String getSupportRepositoryClass() {
        return "org.springframework.data.repository.CrudRepository";
    }
}
