package cn.procsl.ping.boot.domain.support.exector;

import cn.procsl.ping.boot.domain.support.querydsl.QueryDslPersistenceRepository;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.EntityPathResolver;

import javax.persistence.EntityManager;

/**
 * @author procsl
 * @date 2020/04/13
 */
@RequiredArgsConstructor
class QueryDslPersistenceExecutor<T, ID> implements QueryDslPersistenceRepository<T, ID> {

    final JpaEntityInformation<T, ?> entityInformation;

    final EntityManager em;

    final PersistenceProvider provider;

    final CrudMethodMetadata metadata;

    final EscapeCharacter escapeCharacter;

    final EntityPathResolver resolver;

    final EntityPath<T> fullPath;

    final Querydsl querydsl;

    public QueryDslPersistenceExecutor(JpaEntityInformation<T, ?> entityInformation,
                                       EntityManager em,
                                       CrudMethodMetadata metadata,
                                       EscapeCharacter escapeCharacter,
                                       EntityPathResolver resolver
    ) {
        this.entityInformation = entityInformation;
        this.em = em;
        this.provider = PersistenceProvider.fromEntityManager(em);
        this.metadata = metadata;
        this.escapeCharacter = escapeCharacter;
        this.resolver = resolver;
        this.fullPath = resolver.createPath(entityInformation.getJavaType());
        this.querydsl = new Querydsl(em, new PathBuilder<T>(fullPath.getType(), fullPath.getMetadata()));
    }

    @Override
    public long update(T entity, Predicate predicate) {
        return 0;
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
