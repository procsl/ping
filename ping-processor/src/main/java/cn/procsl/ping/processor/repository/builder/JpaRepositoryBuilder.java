package cn.procsl.ping.processor.repository.builder;

import cn.procsl.ping.processor.repository.EntityAndIdRepositoryBuilder;
import cn.procsl.ping.processor.repository.RepositoryBuilder;
import com.google.auto.service.AutoService;


/**
 * 创建
 *
 * @author procsl
 * @date 2020/06/21
 */
@AutoService(RepositoryBuilder.class)
public class JpaRepositoryBuilder extends EntityAndIdRepositoryBuilder {
    @Override
    protected String getSupportRepositoryClass() {
        return "org.springframework.data.jpa.repository.JpaRepository";
    }
}
