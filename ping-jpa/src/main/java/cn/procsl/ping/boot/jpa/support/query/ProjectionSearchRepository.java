package cn.procsl.ping.boot.jpa.support.query;

import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface ProjectionSearchRepository {

    /**
     * 通过指定query dto生成查询并,返回查询结果
     *
     * @param query   查询DTO
     * @param mapping 映射的结果集
     * @param <Q>     查询DTO
     * @param <R>     返回结果DTO
     * @return 返回的结果集
     */
    <Q, R> List<R> search(Q query, Class<R> mapping);

}
