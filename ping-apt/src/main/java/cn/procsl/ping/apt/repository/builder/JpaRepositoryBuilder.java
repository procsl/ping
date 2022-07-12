package cn.procsl.ping.apt.repository.builder;

import cn.procsl.ping.apt.repository.EntityAndIdRepositoryBuilder;
import cn.procsl.ping.apt.repository.RepositoryBuilder;
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
