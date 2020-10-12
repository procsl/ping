package cn.procsl.ping.apt.processor.builder;

import cn.procsl.ping.apt.processor.EntityAndIdRepositoryBuilder;

/**
 * @author procsl
 * @date 2020/06/21
 */
public class CrudRepositoryBuilder extends EntityAndIdRepositoryBuilder {
    @Override
    protected Class<?> getSupportRepositoryClass() throws ClassNotFoundException {
        return Class.forName("org.springframework.data.repository.CrudRepository");
    }
}
