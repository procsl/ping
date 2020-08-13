package cn.procsl.ping.boot.domain.processor.builder;

import cn.procsl.ping.boot.domain.processor.EntityAndIdRepositoryBuilder;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 创建 {@link org.springframework.data.jpa.repository.JpaRepository}
 *
 * @author procsl
 * @date 2020/06/21
 */
public class JpaRepositoryBuilder extends EntityAndIdRepositoryBuilder {
    @Override
    protected Class<JpaRepository> getSupportRepositoryClass() {
        return JpaRepository.class;
    }
}
