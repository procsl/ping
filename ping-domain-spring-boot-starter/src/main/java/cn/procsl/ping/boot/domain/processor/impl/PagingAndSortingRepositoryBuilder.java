package cn.procsl.ping.boot.domain.processor.impl;

import cn.procsl.ping.boot.domain.processor.EntityAndIdRepositoryBuilder;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * 创建 {@link org.springframework.data.repository.PagingAndSortingRepository }
 *
 * @author procsl
 * @date 2020/06/21
 */
public class PagingAndSortingRepositoryBuilder extends EntityAndIdRepositoryBuilder {

    @Override
    protected Class getSupportRepositoryClass() {
        return PagingAndSortingRepository.class;
    }
}
