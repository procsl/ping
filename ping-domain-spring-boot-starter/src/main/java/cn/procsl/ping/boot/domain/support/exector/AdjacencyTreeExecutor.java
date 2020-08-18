package cn.procsl.ping.boot.domain.support.exector;

import cn.procsl.ping.boot.domain.business.Operator;
import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode;
import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode;
import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.business.utils.CollectionUtils;
import cn.procsl.ping.boot.domain.business.utils.ObjectUtils;
import cn.procsl.ping.business.exception.BusinessException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.isNull;

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


    final JpaEntityInformation<E, ID> entityInformation;

    final EntityManager entityManager;

    final PersistenceProvider provider;

    final EscapeCharacter escapeCharacter;

    final @Nullable
    CrudMethodMetadata metadata;

    final Class<E> javaType;

    final Class<ID> idType;

//    final Class<P> pathNodeType;

    final String QUERY_PARENTS_JPQL;

    final String QUERY_CHILDREN_JPQL;

    final String QUERY_DIRECT_JPQL;

    final String QUERY_DEPTH_JPQL;

    final String QUERY_MAX_DEPTH_JPQL;

    final String QUERY_DEPTH_NODE_JPQL;

    final String QUERY_EXISTS_ID_JPQL;

    final String QUERY_LINKS_JPQL;

    final String QUERY_PARENTS_ID_JPQL;

    public AdjacencyTreeExecutor(JpaEntityInformation<E, ID> entityInformation,
                                 EntityManager entityManager,
                                 EscapeCharacter escapeCharacter,
                                 @Nullable CrudMethodMetadata metadata) {
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
        this.escapeCharacter = escapeCharacter;
        this.metadata = metadata;
        this.provider = PersistenceProvider.fromEntityManager(entityManager);

        // 实体类型
        javaType = this.entityInformation.getJavaType();

        // 主键类型
        idType = this.entityInformation.getIdType();

        // 路径节点类型
//        pathNodeType = this.findPathNodType(javaType);

        String name = javaType.getName();
        this.QUERY_PARENTS_JPQL = String.format("select tree from %s as tree where tree.id in (select b.pathId from %s as a inner join a.path as b where b.id =:parent_id) order by tree.depth asc", name, name);

        this.QUERY_CHILDREN_JPQL = String.format("select tree from %s as tree where tree.parentId =:id order by tree.id", name);

        this.QUERY_DIRECT_JPQL = String.format("select tree from %s as tree where tree.id in (select a.parentId from %s as a where id=:id)", name, name);

        this.QUERY_DEPTH_JPQL = String.format("select tree.depth from %s as tree where tree.id=:id", name);

        this.QUERY_MAX_DEPTH_JPQL = String.format("select a.depth from %s as a inner join a.path as b where b.pathId =:id order by a.depth desc", name);

        this.QUERY_DEPTH_NODE_JPQL = String.format("select tree from %s as tree inner join tree.path as b where b.pathId =:id and tree.depth ", name);

        this.QUERY_EXISTS_ID_JPQL = String.format("select count(a.id) from %s as a where a.id=:id", name);

        this.QUERY_LINKS_JPQL = String.format("select a.parentId from %s as a inner join a.path as b where b.pathId in (:start,:end) and (a.parentId = :start or a.parentId = :end)  group by a.id having count(a.id) >= 2", name);

        this.QUERY_PARENTS_ID_JPQL = String.format("select tree.id from %s as tree where tree.id in (select b.pathId from %s as a inner join a.path as b where b.id =:parent_id) order by tree.depth asc", name, name);
    }

    @Override
    public Stream<E> getParents(@NonNull ID id) {
        val tmp = this.entityManager
                .createQuery(QUERY_PARENTS_JPQL, javaType)
                .setLockMode(LockModeType.PESSIMISTIC_READ)
                .setFlushMode(FlushModeType.COMMIT)
                .setParameter("parent_id", id)
                .getResultStream();
        return tmp;
    }

    @Override
    public Stream<ID> getParentIds(ID id) {
        val tmp = this.entityManager
                .createQuery(QUERY_PARENTS_ID_JPQL, idType)
                .setLockMode(LockModeType.PESSIMISTIC_READ)
                .setFlushMode(FlushModeType.COMMIT)
                .setParameter("parent_id", id)
                .getResultStream();
        return (Stream<ID>) tmp;
    }

    @Override
    public Stream<E> getChildren(@NonNull ID id) {
        Stream<E> tmp = this.entityManager
                .createQuery(this.QUERY_CHILDREN_JPQL, javaType)
                .setLockMode(LockModeType.PESSIMISTIC_READ)
                .setFlushMode(FlushModeType.COMMIT)
                .setParameter("id", id)
                .getResultStream();
        return tmp;
    }

    @Override
    public Optional<E> getDirectParent(@NonNull ID id) {
        return this.entityManager.createQuery(this.QUERY_DIRECT_JPQL, javaType)
                .setParameter("id", id)
                .setLockMode(LockModeType.PESSIMISTIC_READ)
                .setFlushMode(FlushModeType.AUTO)
                .setMaxResults(1)
                .getResultStream().findFirst();
    }

    @Override
    public int getDepth(@NonNull ID id) {
        List<Integer> result = this.entityManager.createQuery(this.QUERY_DEPTH_JPQL, Integer.class)
                .setParameter("id", id)
                .setMaxResults(1)
                .setLockMode(LockModeType.PESSIMISTIC_READ)
                .setFlushMode(FlushModeType.COMMIT)
                .getResultList();
        return CollectionUtils.isEmpty(result) ? -1 : result.get(0);
    }

    @Override
    public int findMaxDepth(@NonNull ID id) {
        List<Integer> list = this.entityManager
                .createQuery(this.QUERY_MAX_DEPTH_JPQL, Integer.class)
                .setParameter("id", id)
                .setMaxResults(1)
                .setLockMode(LockModeType.PESSIMISTIC_READ)
                .setFlushMode(FlushModeType.AUTO)
                .getResultList();
        return CollectionUtils.isEmpty(list) ? -1 : list.get(0);
    }

    @Override
    public Stream<E> findDepthNodes(@NonNull ID id, @NonNull Integer depth, Operator operator, Sort.Direction direction) {
        if (direction == null) {
            direction = Sort.Direction.ASC;
        }
        if (operator == null) {
            operator = Operator.GE;
        }
        String jpql = this.QUERY_DEPTH_NODE_JPQL + operator.getOperator() + " :depth order by tree.depth " + direction.name();
        return this.entityManager
                .createQuery(jpql, javaType)
                .setParameter("id", id)
                .setParameter("depth", depth)
                .setLockMode(LockModeType.PESSIMISTIC_READ)
                .setFlushMode(FlushModeType.COMMIT)
                .getResultStream();
    }

    /**
     * 移动目标节点至源节点下
     * 算法思路
     * 首先加载源节点
     *
     * @param source 源节点
     * @param target 目标节点
     */
    @Override
    public void moveTo(@NonNull ID source, @NonNull ID target) {
        if (ObjectUtils.nullSafeEquals(source, target)) {
            return;
        }

        // 如果target的节点为source的父节点
        ID childId = findChildId(source, target);
        if (ObjectUtils.nullSafeEquals(childId, source)) {
            throw new BusinessException("不可将父节点附加至自身子节点");
        }

        E sourceNode = this.entityManager.find(javaType, source, LockModeType.PESSIMISTIC_READ);
        if (isNull(source)) {
            throw new IllegalArgumentException("Source node not found!");
        }

        E targetNode = this.entityManager.find(javaType, target, LockModeType.PESSIMISTIC_WRITE);
        if (isNull(targetNode)) {
            throw new IllegalArgumentException("Target node not found!");
        }

        // 递归修改
        this.recursion(changeAndMerge(targetNode, sourceNode), 0);
        entityManager.flush();
    }

    protected E changeAndMerge(E chile, E parent) {
        chile.changeParent(parent);
        return entityManager.merge(chile);
    }

    protected void recursion(E parent, int context) {
        if (context >= 50) {
            entityManager.flush();
        }
        // 加载直接子节点
        Stream<E> children = this.getChildren(parent.getId());
        // 修改子节点, 修改且合并
        children.map(item -> changeAndMerge(item, parent))
                // 递归子节点, 属于深度优先递归
                .forEach(item -> recursion(item, context));
    }

    protected boolean existsById(ID id) {
        Long count = this.entityManager
                .createQuery(this.QUERY_EXISTS_ID_JPQL, Long.class)
                .setParameter("id", id)
                .setLockMode(LockModeType.NONE)
                .getSingleResult();
        return count != 0;
    }

    @Override
    public void remove(@NonNull ID id) {

    }

    @Override
    public Stream<E> findLinks(ID start, ID end) {
        ID id = this.findChildId(start, end);
        if (id == null) {
            return Stream.empty();
        }
        return this.getParents(id);
    }

    @Override
    public Stream<ID> findLinkIds(ID start, ID end) {
        ID id = this.findChildId(start, end);
        if (id == null) {
            return Stream.empty();
        }
        return this.getParentIds(id);
    }

    @Override
    public Integer calcDepth(@NonNull ID source, @NonNull ID target) {
        if (ObjectUtils.nullSafeEquals(source, target)) {
            return 0;
        }

        // 首先判断是否存在关系
        ID id = findChildId(source, target);
        if (id != null) {
            // 计算节点相对值
            int sourceDepth = this.getDepth(source);
            int targetDepth = this.getDepth(target);
            return sourceDepth - targetDepth;
        }
        return null;
    }

    @Override
    public ID findChildId(ID source, ID target) {
        List<ID> tmp =
                this.entityManager
                        .createQuery(this.QUERY_LINKS_JPQL, this.idType)
                        .setParameter("start", source)
                        .setParameter("end", target)
                        .setLockMode(LockModeType.NONE)
                        .setMaxResults(1)
                        .getResultList();
        return tmp.get(0);
    }

    /**
     * 查询 PathNode 节点类型
     *
     * @param javaType 实体类型
     * @return PathNode 类型
     */
    @SuppressWarnings("unchecked")
    protected Class<? extends AdjacencyPathNode> findPathNodType(Class<?> javaType) {

        ResolvableType entityType = ResolvableType.forClass(javaType, AdjacencyNode.class);
        if (entityType == null) {
            throw new UnsupportedOperationException("Not implement " + AdjacencyNode.class.getName());
        }

        ResolvableType[] types = null;
        for (ResolvableType inf : entityType.getInterfaces()) {
            if (AdjacencyNode.class.isAssignableFrom(inf.getRawClass())) {
                types = inf.getGenerics();
                break;
            }
        }

        if (types == null || types.length == 0) {
            throw new UnsupportedOperationException("Not found:" + AdjacencyPathNode.class.getName());
        }

        for (ResolvableType type : types) {
            Class<?> pathNodeType = type.getRawClass();
            boolean bool = AdjacencyPathNode.class.isAssignableFrom(pathNodeType);
            if (bool) {
                return (Class<? extends AdjacencyPathNode>) pathNodeType;
            }
        }
        throw new UnsupportedOperationException("Not found:" + AdjacencyPathNode.class.getName());
    }
}
