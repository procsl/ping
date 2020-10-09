package cn.procsl.ping.boot.domain.support.executor;

import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode;
import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode;
import cn.procsl.ping.boot.domain.business.tree.model.QAdjacencyNode;
import cn.procsl.ping.boot.domain.business.tree.model.QAdjacencyPathNode;
import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.business.utils.CollectionUtils;
import cn.procsl.ping.boot.domain.business.utils.ObjectUtils;
import cn.procsl.ping.boot.domain.support.SimplePathResolver;
import cn.procsl.ping.business.domain.DomainId;
import cn.procsl.ping.business.exception.BusinessException;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.util.ArrayUtils;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.AbstractJPAQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.NonUniqueResultException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static javax.persistence.LockModeType.PESSIMISTIC_READ;

/**
 * 邻接树数据库操作
 *
 * @author procsl
 * @date 2020/07/31
 */
@Slf4j
@NoRepositoryBean
@Transactional(readOnly = true)
class AdjacencyTreeExecutor<
    E extends AdjacencyNode<ID, P>,
    ID extends Serializable,
    P extends AdjacencyPathNode<ID>>
    implements AdjacencyTreeRepository<E, ID, P> {


    final EntityManager entityManager;

    final EscapeCharacter escapeCharacter;

    final CrudMethodMetadata metadata;

    final Class<E> javaType;

    final Class<ID> idType;

    final JPAQueryFactory jpaQueryFactory;

    final QAdjacencyNode Q;

    final QAdjacencyPathNode P;

    final Querydsl querydsl;

    final JpaEntityInformation<E, ID> entityInformation;

    final QueryHints queryHint;

    final String query_depth_jpql;

    final String query_max_depth_jpql;

    public AdjacencyTreeExecutor(JpaEntityInformation<E, ID> entityInformation,
                                 EntityManager entityManager,
                                 EscapeCharacter escapeCharacter,
                                 CrudMethodMetadata metadata,
                                 BeanFactory beanFactory,
                                 EntityPathResolver entityPathResolver
    ) {

        {
            this.entityManager = entityManager;
            this.escapeCharacter = escapeCharacter;
            this.metadata = metadata;
            this.jpaQueryFactory = beanFactory.getBean(JPAQueryFactory.class);
            this.entityInformation = entityInformation;
            this.javaType = entityInformation.getJavaType();
            this.idType = entityInformation.getIdType();

            this.Q = new QAdjacencyNode(entityPathResolver.createPath(javaType));

            Class<P> type = this.getNodeType(javaType);

            SimplePathResolver resolver = beanFactory.getBeanProvider(SimplePathResolver.class).getIfAvailable(() -> SimplePathResolver.INSTANCE);
            this.P = new QAdjacencyPathNode(resolver.createPath(type));

            this.querydsl = new Querydsl(entityManager, new PathBuilder<>(javaType, this.Q.getMetadata()));
            queryHint = metadata == null ? QueryHints.NoHints.INSTANCE : DefaultQueryHints.of(entityInformation, metadata);
        }

        // 方便折叠
        {
            String name = javaType.getName();

            this.query_depth_jpql = String.format("select tree.depth from %s as tree where tree.id=:id", name);

            this.query_max_depth_jpql = String.format("select a.depth from %s as a inner join a.path as b where b.pathId =:id order by a.depth desc", name);
        }
    }


    @Override
    public <Projection> Projection findOne(@NonNull Expression<Projection> select, Predicate... predicates) throws NonUniqueResultException {
        JPAQuery<Projection> query = this.jpaQueryFactory.select(select).from(Q);
        this.prop(query, queryHint.withFetchGraphs(this.entityManager), predicates);
        query = this.metadata == null ? query : query.setLockMode(this.metadata.getLockModeType());
        return query.fetchOne();
    }

    @Override
    public <Projection> Stream<Projection> findAll(@NonNull Expression<Projection> select, Predicate... predicates) {
        JPAQuery<Projection> query = this.jpaQueryFactory.select(select).from(Q);
        this.prop(query, this.queryHint.withFetchGraphs(this.entityManager), predicates);
        query = this.metadata == null ? query : query.setLockMode(this.metadata.getLockModeType());
        return this.convertTo(select, query.createQuery().getResultStream());
    }

    @Override
    public <Projection> Page<Projection> findAll(@NonNull Expression<Projection> select,
                                                 @NonNull Pageable pageable, Predicate... predicates) {
        JPQLQuery<Projection> query = createQuery(predicates).select(select).from(Q);
        JPQLQuery<Projection> res = querydsl.applyPagination(pageable, query);
        return PageableExecutionUtils.getPage(res.fetch(), pageable, query::fetchCount);
    }


    @Override
    @SuppressWarnings("unchecked")
    public <Projection> Stream<Projection> getParents(Expression<Projection> select, @NonNull ID id, Predicate... predicates) {
        // 查询 pathId = id的
        JPAQuery<ID> subQuery = (JPAQuery<ID>) this.jpaQueryFactory
            .select(P.pathId)
            .from(Q)
            .innerJoin(Q)
            .innerJoin(Q.path, P)
            .where(P.id.eq(id))
            .distinct();

        // 对于查询主键的, 特殊处理直接返回
        if (select.equals(Q.id)) {
            // 设置相关属性, 插入自定义查询条件
            subQuery.orderBy(P.seq.asc());
            this.prop(subQuery, this.queryHint.withFetchGraphs(this.entityManager), predicates);
            subQuery = this.metadata == null ? subQuery : subQuery.setLockMode(this.metadata.getLockModeType());
            return subQuery.createQuery().getResultStream();
        }

        JPAQuery<Projection> result = this.jpaQueryFactory
            .select(select)
            .from(Q)
            .where(Q.id.in(subQuery))
            .orderBy(Q.depth.asc());
        // 设置相关属性, 插入自定义查询条件
        this.prop(result, this.queryHint.withFetchGraphs(this.entityManager), predicates);
        result = this.metadata == null ? result : result.setLockMode(this.metadata.getLockModeType());

        Stream stream = result
            .createQuery()
            .getResultStream();
        return this.convertTo(select, stream);
    }

    @Override
    public <Projection> Page<Projection> getParents(@NonNull Expression<Projection> select,
                                                    @NonNull Pageable pageable,
                                                    @NonNull ID id, Predicate... predicates) {
        JPQLQuery<ID> subQuery =
            (JPQLQuery<ID>) createQuery(predicates)
                .select(P.pathId)
                .from(Q)
                .innerJoin(Q)
                .innerJoin(Q.path, P)
                .where(P.id.eq(id))
                .distinct();

        if (select.equals(Q.id)) {
            subQuery.orderBy(P.seq.asc());
            JPQLQuery<ID> res = querydsl.applyPagination(pageable, subQuery);
            return (Page<Projection>) PageableExecutionUtils.getPage(res.fetch(), pageable, res::fetchCount);
        }

        JPQLQuery<Projection> query = createQuery(predicates)
            .select(select)
            .from(Q)
            .where(Q.id.in(subQuery))
            .orderBy(Q.depth.asc());

        JPQLQuery<Projection> res = querydsl.applyPagination(pageable, query);
        return PageableExecutionUtils.getPage(res.fetch(), pageable, query::fetchCount);
    }

    @Override
    public <Projection> Stream<Projection> getDirectChildren(@NonNull Expression<Projection> select, @NonNull ID id, Predicate... predicates) {
        JPAQuery<Projection> query = this.jpaQueryFactory.select(select).from(Q).where(Q.parentId.eq(id).and(Q.id.ne(id)));
        this.prop(query, this.queryHint.withFetchGraphs(this.entityManager), predicates);
        query = this.metadata == null ? query : query.setLockMode(this.metadata.getLockModeType());
        Stream stream = query.createQuery().getResultStream();
        return this.convertTo(select, stream);
    }

    @Override
    public <Projection> Page<Projection> getDirectChildren(@NonNull Expression<Projection> select,
                                                           @NonNull Pageable pageable,
                                                           @NonNull ID id, Predicate... predicates) {
        JPQLQuery<Projection> query = this.createQuery(predicates)
            .select(select)
            .from(Q)
            .where(P.pathId.eq(id).and(P.id.ne(id)));
        JPQLQuery<Projection> res = querydsl.applyPagination(pageable, query);
        return PageableExecutionUtils.getPage(res.fetch(), pageable, query::fetchCount);
    }

    @Override
    public <Projection> Stream<Projection> getAllChildren(@NonNull Expression<Projection> select,
                                                          @NonNull ID id, Predicate... predicates) {
        JPAQuery<Projection> query = this.jpaQueryFactory
            .select(select)
            .from(Q)
            .innerJoin(Q)
            .innerJoin(Q.path, P)
            .where(P.pathId.eq(id).and(P.id.ne(id)))
            .orderBy(Q.depth.asc());
        this.prop(query, this.queryHint.withFetchGraphs(this.entityManager), predicates);
        query = this.metadata == null ? query : query.setLockMode(this.metadata.getLockModeType());
        return this.convertTo(select, query.createQuery().getResultStream());
    }

    @Override
    public <Projection> Page<Projection> getAllChildren(@NonNull Expression<Projection> select,
                                                        @NonNull Pageable pageable,
                                                        @NonNull ID id, Predicate... predicates) {
        JPQLQuery<Projection> query = this.createQuery(predicates)
            .select(select)
            .from(Q)
            .innerJoin(Q)
            .innerJoin(Q.path, P)
            .where(P.pathId.eq(id).and(P.id.ne(id)));
        JPQLQuery<Projection> res = querydsl.applyPagination(pageable, query);
        return PageableExecutionUtils.getPage(res.fetch(), pageable, query::fetchCount);
    }

    @Override
    public <Projection> Optional<Projection> getDirectParent(@NonNull Expression<Projection> select, @NonNull ID id) {
        @SuppressWarnings("unchecked")
        JPAQuery<ID> subQuery = (JPAQuery<ID>) this.jpaQueryFactory
            .select(Q.parentId)
            .from(Q)
            .where(Q.id.eq(id));

        JPAQuery<Projection> query = this.jpaQueryFactory
            .select(select)
            .from(Q)
            .where(Q.id.in(subQuery))
            .limit(1);
        this.prop(query, queryHint.withFetchGraphs(this.entityManager));
        query = this.metadata == null ? query : query.setLockMode(this.metadata.getLockModeType());
        List<Projection> t = query.fetch();
        if (t.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(t.get(0));
    }

    @Override
    public int getDepth(@NonNull ID id) {
        if (log.isDebugEnabled()) {
            log.debug("获取指定节点:{}的深度", id);
        }
        List<Integer> result = this.entityManager.createQuery(this.query_depth_jpql, Integer.class)
            .setParameter("id", id)
            .setMaxResults(1)
            .setLockMode(PESSIMISTIC_READ)
            .setFlushMode(FlushModeType.COMMIT)
            .getResultList();
        return CollectionUtils.isEmpty(result) ? -1 : result.get(0);
    }

    @Override
    public int findMaxDepth(@NonNull ID id) {
        if (log.isDebugEnabled()) {
            log.debug("获取指定节点:{}的的最大子节点深度", id);
        }
        List<Integer> list = this.entityManager
            .createQuery(this.query_max_depth_jpql, Integer.class)
            .setParameter("id", id)
            .setMaxResults(1)
            .setLockMode(PESSIMISTIC_READ)
            .setFlushMode(FlushModeType.AUTO)
            .getResultList();
        return CollectionUtils.isEmpty(list) ? -1 : list.get(0);
    }

    @Override
    public void mount(ID root, @NonNull ID target) {
        if (ObjectUtils.nullSafeEquals(root, target)) {
            return;
        }

        E sourceNode = null;
        if (root != null) {
            // 如果target的节点为source的父节点
            @SuppressWarnings("unchecked")
            ID childId = (ID) calcChildren(Q.id, root, target);
            if (ObjectUtils.nullSafeEquals(childId, root)) {
                throw new BusinessException("不可将父节点附加至自身子节点");
            }

            sourceNode = this.entityManager.find(javaType, root, PESSIMISTIC_READ);
            if (isNull(sourceNode)) {
                throw new IllegalArgumentException("找不到源节点对应的实体");
            }
        }

        E targetNode = this.entityManager.find(javaType, target, PESSIMISTIC_READ);
        if (isNull(targetNode)) {
            throw new IllegalArgumentException("Target node not found!");
        }

        // 递归修改
        log.info("开始挂载节点: root:{} ---> target:{}", root, target);
        this.recursion(changeAndMerge(sourceNode, targetNode), 0);
    }

    protected E changeAndMerge(E parent, E child) {
        if (log.isDebugEnabled()) {
            log.debug("开始修改节点:child:{}, parent:{}", child.getId(), parent == null ? null : parent.getId());
        }
        child.changeParent(parent);
        return entityManager.merge(child);
    }

    // 这里是可以优化的, 但是懒得做了
    // 可以使用jpql更新部分字段, 然后使用部分更新可以避免再次更新主表
    protected void recursion(E parent, int times) {
        final int finalTimes = times + 1;

        log.info("进入递归方法:标识符为{}", times);
        if (log.isDebugEnabled()) {
            log.debug("刷入上一批id:{}修改的数据", parent == null ? null : parent.getId());
        }
        entityManager.flush();
        // 加载直接子节点
        assert parent != null;

        @Cleanup
        @SuppressWarnings("unchecked")
        Stream<E> children = this.getDirectChildren((EntityPathBase<E>) Q, parent.getId());
        // 修改子节点, 修改且合并
        children.map(child -> changeAndMerge(parent, child))
            // 递归子节点, 属于深度优先递归
            .forEach(item -> recursion(item, finalTimes));
        log.info("退出递归方法,标识符为:{}", times);
    }

    @Override
    @SuppressWarnings("unchecked")
    public int remove(@NonNull ID id) {
        @Cleanup
        Stream<E> stream = (Stream<E>) this.getAllChildren(Q, id);
        AtomicInteger count = new AtomicInteger(0);
        stream.forEach(item -> {
            if (log.isTraceEnabled()) {
                log.trace("删除:{}", item.getId());
            }
            // 之所以不用jpql, 是因为方便生命周期的方法拦截做逻辑注入,而且jpql实现复杂
            this.entityManager.remove(item);
            count.getAndIncrement();
        });
        this.entityManager.flush();
        log.info("删除成功, 影响条数为:{}", count.get());
        return count.get();
    }

    @Override
    public <Projection> Stream<Projection> findLinks(@NonNull Expression<Projection> select, @NonNull ID start, @NonNull ID end, Predicate... predicates) {
        // 获取父节点的信息, id, 高度
        Tuple tuple = this.calcParent(com.querydsl.core.types.Projections.tuple(Q.id, Q.depth), start, end);
        if (tuple == null || tuple.size() == 0) {
            return Stream.empty();
        }

        // 指定的高度必须大于等于父节点当前的高度
        BooleanExpression cond = Q.depth.goe(tuple.get(Q.depth));
        ID id = tuple.get(Q.id).equals(start) ? end : start;

        if (predicates != null && predicates.length > 0) {
            Predicate[] newPred = Arrays.copyOf(predicates, predicates.length + 1);
            newPred[predicates.length] = cond;
            return this.getParents(select, id, newPred);
        }

        return this.getParents(select, id, cond);
    }

    @Override
    public <Projections extends DomainId<ID>> List<Projections> searchAll(@NonNull Expression<Projections> selector,
                                                                          @NonNull List<?> nodes,
                                                                          @NonNull Function<Integer, Predicate> fun,
                                                                          @NonNull boolean isFull
    ) throws IllegalArgumentException {
        if (nodes.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<Projections> list = new ArrayList<>(nodes.size());

        ID parentId = null;
        for (int i = 0; i < nodes.size(); i++) {
            BooleanExpression where = parentId == null ? Q.parentId.eq(Q.id) : Q.parentId.eq(parentId);

            JPAQuery<Projections> query = this.jpaQueryFactory.select(selector).from(Q);

            QueryHints hint = queryHint.withFetchGraphs(this.entityManager);

            this.prop(query, hint, where, Q.depth.eq(i), fun.apply(i));

            query = this.metadata == null ? query : query.setLockMode(this.metadata.getLockModeType());

            Projections projection = query.fetchOne();
            if (projection == null) {
                break;
            }
            parentId = projection.getId();
            if (parentId == null) {
                throw new IllegalArgumentException("必须设置ID项");
            }
            list.add(i, projection);
        }
        if (isFull) {
            return list.size() == nodes.size() ? list : Collections.emptyList();
        }
        return list;
    }

    public <Projections extends DomainId<ID>> Projections searchOne(@NonNull Expression<Projections> selector,
                                                                    @NonNull List<?> nodes,
                                                                    @NonNull Function<Integer, Predicate> fun,
                                                                    @NonNull boolean isFull) throws IllegalArgumentException {
        List<Projections> tmp = this.searchAll(selector, nodes, fun, isFull);
        return tmp.isEmpty() ? null : tmp.get(tmp.size() - 1);
    }


    @Override
    public <Projection> Page<Projection> findLinks(@NonNull Expression<Projection> select,
                                                   @NonNull Pageable pageable,
                                                   @NonNull ID start,
                                                   @NonNull ID end, Predicate... predicates) {

        // 获取父节点的信息, id, 高度
        Tuple tuple = this.calcParent(com.querydsl.core.types.Projections.tuple(Q.id, Q.depth), start, end);
        if (tuple == null || tuple.size() == 0) {
            return Page.empty();
        }

        // 指定的高度必须大于等于父节点当前的高度
        BooleanExpression cond = Q.depth.goe(tuple.get(Q.depth));
        @SuppressWarnings("unchecked")
        ID id = tuple.get(Q.id).equals(start) ? end : start;

        if (predicates != null && predicates.length > 0) {
            Predicate[] newPred = Arrays.copyOf(predicates, predicates.length + 1);
            newPred[predicates.length] = cond;
            return this.getParents(select, pageable, id, newPred);
        }
        return this.getParents(select, pageable, id, cond);
    }

    @Override
    public Integer calcDepth(@NonNull ID source, @NonNull ID target) {
        if (ObjectUtils.nullSafeEquals(source, target)) {
            return 0;
        }

        // 首先判断是否存在关系
        Tuple info = calcChildren(com.querydsl.core.types.Projections.tuple(Q.id, Q.depth), source, target);
        if (info == null || info.size() == 0) {
            return null;
        }
        // 计算节点相对值
        @SuppressWarnings("unchecked")
        ID id = (ID) info.get(Q.id);
        Integer depth = info.get(Q.depth);

        assert id != null;
        assert depth != null;

        return id.equals(source) ? depth - this.getDepth(target) : this.getDepth(source) - depth;
    }

    @Override
    public <Projection> Projection calcChildren(@NonNull Expression<Projection> select, @NonNull ID pre, @NonNull ID next) {
        return this.calc(select, pre, next, 2);
    }

    protected <Projection> Projection calc(@NonNull Expression<Projection> select, @NonNull ID pre, @NonNull ID next, long having) {
        JPAQuery<Projection> query = this.jpaQueryFactory
            .select(select)
            .from(Q)
            .innerJoin(Q)
            .innerJoin(Q.path, P)
            .where(
                P.pathId.in(pre, next).and(
                    Q.id.eq(pre).or(Q.id.eq(next))
                )
            )
            .groupBy(Q.id)
            .having(Q.id.count().eq(having))
            .limit(1);
        this.prop(query, this.queryHint.withFetchGraphs(this.entityManager));
        query = this.metadata == null ? query : query.setLockMode(this.metadata.getLockModeType());
        List<Projection> list = query.fetch();

        if (list.isEmpty()) {
            return null;
        }

        return list.get(0);
    }

    public <Projection> Projection calcParent(@NonNull Expression<Projection> select, @NonNull ID pre, @NonNull ID next) {
        return this.calc(select, pre, next, 1);
    }

    @Override
    public <Projection> Stream<Projection> getRoots(@NonNull Expression<Projection> select, Predicate... predicates) {

        JPAQuery<Projection> query = this.jpaQueryFactory
            .select(select).from(Q).where();

        QueryHints hints = queryHint.withFetchGraphs(entityManager);
        this.prop(query, hints, this.buildRootCondition(predicates));
        query = this.metadata == null ? query : query.setLockMode(this.metadata.getLockModeType());

        Stream stream = query.createQuery().getResultStream();

        return this.convertTo(select, stream);

    }

    @Override
    public <Projection> Page<Projection> getRoots(@NonNull Expression<Projection> select, @NonNull Pageable pageable, Predicate... predicates) {
        Predicate cond = this.buildRootCondition(predicates);
        JPQLQuery<Projection> query = querydsl.applyPagination(pageable, createQuery(cond).select(select));
        return PageableExecutionUtils.getPage(query.fetch(), pageable, createCountQuery(cond)::fetchCount);
    }

    @Override
    public QAdjacencyNode getQAdjacency() {
        return Q;
    }

    @Override
    public QAdjacencyPathNode getQAdjacencyPath() {
        return P;
    }

    protected Class<P> getNodeType(Class<? super E> clazz) {
        Class<P> nodeClass = findNodeType(clazz);

        if (nodeClass != null) {
            return nodeClass;
        }

        nodeClass = findNodeType(clazz.getGenericSuperclass());
        if (nodeClass != null) {
            return nodeClass;
        }

        nodeClass = this.findNodeType(clazz.getGenericInterfaces());
        if (nodeClass != null) {
            return nodeClass;
        }

        if (clazz.equals(Object.class)) {
            return null;
        }

        // 查找超类的接口
        Class<? super E> superClass = clazz.getSuperclass();
        return this.getNodeType(superClass);
    }

    protected Class<P> findNodeType(Type... types) {
        for (Type type : types) {
            if (!(type instanceof ParameterizedType)) {
                continue;
            }

            Type[] args = ((ParameterizedType) (type)).getActualTypeArguments();
            for (Type arg : args) {
                if (!(arg instanceof Class)) {
                    continue;
                }
                Class c = (Class) arg;
                if (AdjacencyPathNode.class.isAssignableFrom(c)) {
                    return c;
                }
                if (c.isPrimitive()) {
                    continue;
                }

                if (c.isArray()) {
                    continue;
                }

                if (c.isEnum()) {
                    continue;
                }

                if (c.getName().startsWith("javax.")) {
                    continue;
                }
                if (c.getName().startsWith("java.")) {
                    continue;
                }

                return this.findNodeType();
            }
        }
        return null;
    }

    protected Predicate buildRootCondition(Predicate... others) {
        // 根节点条件
        BooleanExpression condition = this
            .Q
            .depth
            .eq(AdjacencyNode.ROOT_DEPTH)
            .or(this.Q.id.eq(this.Q.parentId));

        //自定义条件
        if (!ArrayUtils.isEmpty(others)) {
            for (Predicate other : others) {
                condition = condition.and(other);
            }
        }
        return condition;
    }

    protected JPQLQuery<?> createCountQuery(@Nullable Predicate... predicate) {
        return doCreateQuery(getQueryHintsForCount(), predicate);
    }

    protected QueryHints getQueryHintsForCount() {
        return metadata == null ? QueryHints.NoHints.INSTANCE
            : DefaultQueryHints.of(entityInformation, metadata).forCounts();
    }

    protected AbstractJPAQuery<?, ?> doCreateQuery(QueryHints hints, @Nullable Predicate... predicate) {
        AbstractJPAQuery<?, ?> query = querydsl.createQuery(this.Q);
        prop(query, hints, predicate);
        return query;
    }

    @SuppressWarnings("unchecked")
    protected <Projection> Stream<Projection> convertTo(Expression<Projection> select, Stream stream) {
        if (select instanceof FactoryExpression) {
            return stream.map(
                item -> ((FactoryExpression<Projection>) select).newInstance((Object[]) item)
            );
        }
        return stream;
    }

    protected void prop(AbstractJPAQuery<?, ?> query, QueryHints hints, @Nullable Predicate... predicate) {
        if (predicate != null) {
            query = query.where(predicate);
        }

        for (Map.Entry<String, Object> hint : hints) {
            query.setHint(hint.getKey(), hint.getValue());
        }
    }

    protected JPQLQuery<?> createQuery(Predicate... predicate) {

        AbstractJPAQuery<?, ?> query = doCreateQuery(queryHint.withFetchGraphs(entityManager), predicate);

        if (metadata == null) {
            return query;
        }

        LockModeType type = metadata.getLockModeType();
        return type == null ? query : query.setLockMode(type);
    }

}
