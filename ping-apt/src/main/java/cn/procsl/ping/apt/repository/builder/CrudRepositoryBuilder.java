package cn.procsl.ping.apt.repository.builder;

import cn.procsl.ping.apt.repository.EntityAndIdRepositoryBuilder;

/**
 * @author procsl
 * &#064;date  2020/06/21
 */
//@AutoService(RepositoryBuilder.class)
public class CrudRepositoryBuilder extends EntityAndIdRepositoryBuilder {
    @Override
    protected String getSupportRepositoryClass() {
        return "org.springframework.data.repository.CrudRepository";
    }
}
