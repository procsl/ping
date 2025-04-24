package cn.procsl.ping.boot.jpa.support.query;

import cn.procsl.ping.boot.jpa.domain.page.FormatPage;
import org.springframework.data.domain.Pageable;
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

    /**
     * 按照分页查询
     *
     * @param query   查询DTO
     * @param mapping 映射的结果集
     * @param <Q>     查询DTO
     * @param <R>     返回结果DTO
     * @return 返回的结果集
     */
    <Q, R> FormatPage<R> search(Q query, Class<R> mapping, Pageable pageable);

}
