package cn.procsl.ping.processor.repo.builder;

import cn.procsl.ping.processor.repo.EntityAndIdRepositoryBuilder;
import cn.procsl.ping.processor.repo.RepositoryBuilder;
import com.google.auto.service.AutoService;


/**
 * 创建
 *
 * @author procsl
 * @date 2020/06/21
 */
@AutoService(RepositoryBuilder.class)
public class PagingAndSortingRepositoryBuilder extends EntityAndIdRepositoryBuilder {

    @Override
    protected String getSupportRepositoryClass() {
        return "org.springframework.data.repository.PagingAndSortingRepository";
    }
}
