package cn.procsl.ping.boot.domain.support.querydsl;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

/**
 * @author procsl
 * @date 2020/04/12
 */
@NoRepositoryBean
public interface QueryDslPersistenceRepository<T, ID> extends Repository<T, ID> {

    /**
     * 根据条件 持久化 当前实体
     *
     * @param entity    待 持久化 的实体
     * @param predicate 持久化条件
     * @return 影响行数
     */
    long update(T entity, Predicate predicate);

    /**
     * 根据条件持久化指定的字段
     *
     * @param entity    待持久化的实体
     * @param predicate 持久化条件
     * @param fields    指定的字段
     * @return 返回影响行数
     */
    long update(T entity, Predicate predicate, String... fields);

    /**
     * 根据条件持久化指定实体的字段
     *
     * @param entity    待持久化的实体
     * @param predicate 持久化条件
     * @param fields    持久化的字段
     * @return 影响行数
     */
    long update(T entity, Predicate predicate, Path... fields);

    /**
     * 根据条件持久化指定的非空字段
     *
     * @param entity    待持久化的实例
     * @param predicate 持久化条件
     * @return 影响行数
     */
    long updateNotNull(T entity, Predicate predicate);

    /**
     * 以排除的方式持久化指定的实体
     *
     * @param entity    待持久化实体
     * @param predicate 持久化条件
     * @param fields    排除的字段
     * @return 返回影响行数
     */
    long updateExclude(T entity, Predicate predicate, String... fields);

    /**
     * 以排除的方式持久化指定的实体
     *
     * @param entity    待持久化实体
     * @param predicate 持久化条件
     * @param fields    排除的字段
     * @return 返回影响行数
     */
    long updateExclude(T entity, Predicate predicate, Path... fields);

    /**
     * 根据条件删除指定的实体
     *
     * @param predicate 指定的删除条件
     * @return 返回影响行数
     */
    long delete(Predicate predicate);
}
