package cn.procsl.ping.processor.repo.builder;

import cn.procsl.ping.processor.repo.EntityAndIdRepositoryBuilder;

/**
 * @author procsl
 * @date 2020/06/21
 */
//@AutoService(RepositoryBuilder.class)
public class CrudRepositoryBuilder extends EntityAndIdRepositoryBuilder {
    @Override
    protected String getSupportRepositoryClass() {
        return "org.springframework.data.repository.CrudRepository";
    }
}
