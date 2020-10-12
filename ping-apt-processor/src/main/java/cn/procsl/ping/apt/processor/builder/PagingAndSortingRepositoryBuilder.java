package cn.procsl.ping.apt.processor.builder;

import cn.procsl.ping.apt.processor.EntityAndIdRepositoryBuilder;


/**
 * 创建
 *
 * @author procsl
 * @date 2020/06/21
 */
public class PagingAndSortingRepositoryBuilder extends EntityAndIdRepositoryBuilder {

    @Override
    protected Class<?> getSupportRepositoryClass() throws ClassNotFoundException {
        return Class.forName("org.springframework.data.repository.PagingAndSortingRepository");
    }
}
