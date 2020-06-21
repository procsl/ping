package cn.procsl.ping.boot.data.processor.impl;

import cn.procsl.ping.boot.data.processor.EntityAndIdRepositoryBuilder;
import org.springframework.data.repository.CrudRepository;

/**
 * @author procsl
 * @date 2020/06/21
 */
public class CrudRepositoryBuilder extends EntityAndIdRepositoryBuilder {
    @Override
    protected Class getSupportRepositoryClass() {
        return CrudRepository.class;
    }
}
