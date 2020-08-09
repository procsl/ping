package cn.procsl.ping.boot.domain.support.exector;

import cn.procsl.ping.boot.domain.support.querydsl.QueryDslPersistenceRepository;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;

/**
 * @author procsl
 * @date 2020/04/13
 */
@RequiredArgsConstructor
public class QueryDslPersistenceExecutor<T, ID> implements QueryDslPersistenceRepository<T, ID> {

    final private EntityManager entityManager;

    final private JPAQueryFactory queryFactory;

    final private EntityPath<T> fullPath;

    @Override
    public long update(T entity, Predicate predicate) {
//        new PathBuilder(fullPath.getMetadata(),).as(entity.getClass());
        return queryFactory.update(fullPath).set(fullPath, entity).where(predicate).execute();
    }

    @Override
    public long update(T entity, Predicate predicate, String... fields) {
        return 0;
    }

    @Override
    public long update(T entity, Predicate predicate, Path... fields) {
        return 0;
    }

    @Override
    public long updateNotNull(T entity, Predicate predicate) {
        return 0;
    }

    @Override
    public long updateExclude(T entity, Predicate predicate, String... fields) {
        return 0;
    }

    @Override
    public long updateExclude(T entity, Predicate predicate, Path... fields) {
        return 0;
    }

    @Override
    public long delete(Predicate predicate) {
        return 0;
    }
}
