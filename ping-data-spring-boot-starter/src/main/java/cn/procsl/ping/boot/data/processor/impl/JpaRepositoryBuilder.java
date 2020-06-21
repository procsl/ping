package cn.procsl.ping.boot.data.processor.impl;

import cn.procsl.ping.boot.data.processor.EntityAndIdRepositoryBuilder;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 创建 {@link org.springframework.data.jpa.repository.JpaRepository}
 *
 * @author procsl
 * @date 2020/06/21
 */
public class JpaRepositoryBuilder extends EntityAndIdRepositoryBuilder {
    @Override
    protected Class getSupportRepositoryClass() {
        return JpaRepository.class;
    }
}
