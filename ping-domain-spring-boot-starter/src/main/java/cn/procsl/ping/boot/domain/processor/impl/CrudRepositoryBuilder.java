package cn.procsl.ping.boot.domain.processor.impl;

import cn.procsl.ping.boot.domain.processor.EntityAndIdRepositoryBuilder;
import org.springframework.data.repository.CrudRepository;

/**
 * @author procsl
 * @date 2020/06/21
 */
public class CrudRepositoryBuilder extends EntityAndIdRepositoryBuilder {
    @Override
    protected Class<CrudRepository> getSupportRepositoryClass() {
        return CrudRepository.class;
    }
}
