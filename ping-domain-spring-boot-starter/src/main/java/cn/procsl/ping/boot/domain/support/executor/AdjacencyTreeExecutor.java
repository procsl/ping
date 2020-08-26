package cn.procsl.ping.boot.domain.support.executor;

import cn.procsl.ping.boot.domain.business.common.model.Operator;
import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyNode;
import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode;
import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.business.utils.CollectionUtils;
import cn.procsl.ping.boot.domain.business.utils.ObjectUtils;
import cn.procsl.ping.business.exception.BusinessException;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
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


    final JpaEntityInformation<E, ID> entityInformation;

    final EntityManager entityManager;

    final PersistenceProvider provider;

    final EscapeCharacter escapeCharacter;

    final @Nullable
    CrudMethodMetadata metadata;

    final Class<E> javaType;

    final Class<ID> idType;

    final String query_parents_jpql;

    final String query_children_jpql;

    final String query_children_id_jpql;

    final String query_direct_jpql;

    final String query_depth_jpql;

    final String query_max_depth_jpql;

    final String query_depth_node_jpql;

    final String query_exists_id_jpql;

    final String query_links_jpql;

    final String query_parents_id_jpql;

    final String query_all_children_id_jpql;

    final String query_all_children_jpql;

    final String query_roots_jpql;

    final String query_root_ids_jpql;

    Random rand = new Random();

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

        String name = javaType.getName();
        this.query_parents_jpql = String.format("select tree from %s as tree where tree.id in (select b.pathId from %s as a inner join a.path as b where b.id =:parent_id) order by tree.depth asc", name, name);

        this.query_children_jpql = String.format("select tree from %s as tree where tree.parentId =:id order by tree.id", name);

        this.query_children_id_jpql = String.format("select tree.id from %s as tree where tree.parentId =:id order by tree.id", name);

        this.query_direct_jpql = String.format("select tree from %s as tree where tree.id in (select a.parentId from %s as a where id=:id)", name, name);

        this.query_depth_jpql = String.format("select tree.depth from %s as tree where tree.id=:id", name);

        this.query_max_depth_jpql = String.format("select a.depth from %s as a inner join a.path as b where b.pathId =:id order by a.depth desc", name);

        this.query_depth_node_jpql = String.format("select tree from %s as tree inner join tree.path as b where b.pathId =:id and tree.depth ", name);

        this.query_exists_id_jpql = String.format("select count(a.id) from %s as a where a.id=:id", name);

        this.query_links_jpql = String.format("select a.id from %s as a inner join a.path as b where b.pathId in (:start,:end) and (a.parentId = :start or a.parentId = :end)  group by a.id having count(a.id) >= 2", name);

        this.query_parents_id_jpql = String.format("select tree.id from %s as tree where tree.id in (select b.pathId from %s as a inner join a.path as b where b.id =:parent_id) order by tree.depth asc", name, name);

        this.query_all_children_id_jpql = String.format("select b.id from %s as b inner join b.path as c where c.pathId=:id order by b.depth asc", name);

        this.query_all_children_jpql = String.format("select b from %s as b inner join b.path as c where c.pathId=:id order by b.depth asc", name);

        this.query_roots_jpql = String.format("select a from %s as a where a.id = a.parentId or a.depth = 0", name);

        this.query_root_ids_jpql = String.format("select a.id from %s as a where a.id = a.parentId or a.depth = 0", name);
    }

    @Override
    public Stream<E> getParents(@NonNull ID id) {
        if (log.isDebugEnabled()) {
            log.debug("查询节点:{}的父节点", id);
        }
        return this.entityManager
                .createQuery(query_parents_jpql, javaType)
                .setLockMode(PESSIMISTIC_READ)
                .setFlushMode(FlushModeType.COMMIT)
                .setParameter("parent_id", id)
                .getResultStream();
    }

    @Override
    public Stream<ID> getParentIds(@NonNull ID id) {
        if (log.isDebugEnabled()) {
            log.debug("查询节点:{}的父节点IDs", id);
        }
        return this.entityManager
                .createQuery(query_parents_id_jpql, idType)
                .setLockMode(PESSIMISTIC_READ)
                .setFlushMode(FlushModeType.COMMIT)
                .setParameter("parent_id", id)
                .getResultStream();
    }

    @Override
    public Stream<E> getDirectChildren(@NonNull ID id) {
        if (log.isDebugEnabled()) {
            log.debug("查询节点:{}的直接子节点", id);
        }
        return this.entityManager
                .createQuery(this.query_children_jpql, javaType)
                .setLockMode(PESSIMISTIC_READ)
                .setFlushMode(FlushModeType.COMMIT)
                .setParameter("id", id)
                .getResultStream();
    }

    /**
     * 获取指定子节点的IDs
     *
     * @param id 指定的节点ID
     * @return 返回子节点IDs
     */
    @Override
    public Stream<ID> getDirectChildrenIds(@NonNull ID id) {
        if (log.isDebugEnabled()) {
            log.debug("查询节点:{}的直接子节点IDs", id);
        }
        return this.entityManager
                .createQuery(this.query_children_id_jpql, idType)
                .setLockMode(PESSIMISTIC_READ)
                .setFlushMode(FlushModeType.COMMIT)
                .setParameter("id", id)
                .getResultStream();
    }

    /**
     * 查询所有的子节点, 包含自身节点ID
     *
     * @param id 指定的节点ID
     * @return 返回所有的子节点ID
     */
    @Override
    public Stream<ID> getAllChildrenIds(@NonNull ID id) {
        if (log.isDebugEnabled()) {
            log.debug("查询节点:{}的所有子节点IDs", id);
        }
        return this.entityManager
                .createQuery(this.query_all_children_id_jpql, idType)
                .setParameter("id", id)
                .setLockMode(PESSIMISTIC_READ)
                .setFlushMode(FlushModeType.AUTO)
                .getResultStream();
    }

    /**
     * 获取所有的子节点包含自身节点
     *
     * @param id 指定的节点
     * @return 返回所有的子节点
     */
    @Override
    public Stream<E> getAllChildren(@NonNull ID id) {
        if (log.isDebugEnabled()) {
            log.debug("查询节点:{}的所有子节点", id);
        }
        return this.entityManager.createQuery(this.query_all_children_jpql, javaType)
                .setParameter("id", id)
                .setLockMode(PESSIMISTIC_READ)
                .setFlushMode(FlushModeType.AUTO)
                .getResultStream();
    }

    @Override
    public Optional<E> getDirectParent(@NonNull ID id) {
        if (log.isDebugEnabled()) {
            log.debug("查询节点:{}的直接父节点", id);
        }
        return this.entityManager.createQuery(this.query_direct_jpql, javaType)
                .setParameter("id", id)
                .setLockMode(PESSIMISTIC_READ)
                .setFlushMode(FlushModeType.AUTO)
                .setMaxResults(1)
                .getResultStream().findFirst();
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
    public Stream<E> findDepthNodes(@NonNull ID id, @NonNull Integer depth, Operator operator, Sort.Direction direction) {
        if (direction == null) {
            direction = Sort.Direction.ASC;
        }
        if (operator == null) {
            operator = Operator.GE;
        }
        String jpql = this.query_depth_node_jpql + operator.getOperator() + " :depth order by tree.depth " + direction.name();
        if (log.isDebugEnabled()) {
            log.debug("查询指定节点子节点:{},深度:{},条件:{},排序:{}", id, depth, operator, direction);
        }
        return this.entityManager
                .createQuery(jpql, javaType)
                .setParameter("id", id)
                .setParameter("depth", depth)
                .setLockMode(PESSIMISTIC_READ)
                .setFlushMode(FlushModeType.COMMIT)
                .getResultStream();
    }

    /**
     * 移动目标节点至源节点下
     * 算法思路
     * 首先加载源节点
     *
     * @param root   源节点, 如果源节点为null, 则表示将指定的节点挂在到根节点下
     * @param target 目标节点
     */
    @Override
    public void mount(ID root, @NonNull ID target) {
        if (ObjectUtils.nullSafeEquals(root, target)) {
            return;
        }

        E sourceNode = null;
        if (root != null) {
            // 如果target的节点为source的父节点
            ID childId = calcChildId(root, target);
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
        this.recursion(changeAndMerge(sourceNode, targetNode));
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
    protected void recursion(E parent) {
        long curr = rand.nextLong();
        log.info("进入递归方法:标识符为{}", curr);
        if (log.isDebugEnabled()) {
            log.debug("刷入上一批id:{}修改的数据", parent == null ? null : parent.getId());
        }
        entityManager.flush();
        // 加载直接子节点
        assert parent != null;
        @Cleanup
        Stream<E> children = this.getDirectChildren(parent.getId());
        // 修改子节点, 修改且合并
        children.map(child -> changeAndMerge(parent, child))
                // 递归子节点, 属于深度优先递归
                .forEach(this::recursion);
        log.info("退出递归方法,标识符为:{}", curr);
    }

    @Override
    public int remove(@NonNull ID id) {
        Stream<ID> stream = this.getAllChildrenIds(id);
        AtomicInteger count = new AtomicInteger(0);
        stream.forEach(item -> {
            E refer = this.entityManager.getReference(javaType, item);
            if (log.isTraceEnabled()) {
                log.trace("删除:{}", item);
            }
            this.entityManager.remove(refer);
            count.getAndIncrement();
        });
        this.entityManager.flush();
        log.info("删除成功, 影响条数为:{}", count.get());
        return count.get();
    }

    @Override
    public Stream<E> findLinks(ID start, ID end) {
        ID id = this.calcChildId(start, end);
        if (id == null) {
            return Stream.empty();
        }
        return this.getParents(id);
    }

    @Override
    public Stream<ID> findLinkIds(ID start, ID end) {
        ID id = this.calcChildId(start, end);
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
        ID id = calcChildId(source, target);
        if (id != null) {
            // 计算节点相对值
            int sourceDepth = this.getDepth(source);
            int targetDepth = this.getDepth(target);
            if (log.isDebugEnabled()) {
                log.debug("source:{}的深度为:{}", source, sourceDepth);
                log.debug("target:{}的深度为:{}", target, targetDepth);
            }
            return sourceDepth - targetDepth;
        }
        return null;
    }

    @Override
    public ID calcChildId(ID pre, ID next) {
        List<ID> tmp =
                this.entityManager
                        .createQuery(this.query_links_jpql, this.idType)
                        .setParameter("start", pre)
                        .setParameter("end", next)
                        .setLockMode(LockModeType.NONE)
                        .setMaxResults(1)
                        .getResultList();
        if (CollectionUtils.isEmpty(tmp)) {
            if (log.isDebugEnabled()) {
                log.debug("source:{}与target:{}不存在父子关系", pre, next);
            }
            return null;
        }
        if (log.isDebugEnabled()) {
            log.debug("source:{},target:{}中的子节点为:{}", pre, next, tmp.get(0));
        }
        return tmp.get(0);
    }

    /**
     * 获取root节点列表
     *
     * @return 返回root节点
     */
    @Override
    public Stream<E> getRoots() {
        return this.entityManager.createQuery(this.query_roots_jpql, javaType)
                .setFlushMode(FlushModeType.AUTO)
                .getResultStream();
    }

    /**
     * 获取root ids
     *
     * @return root 节点id
     */
    @Override
    public Stream<ID> getRootIds() {
        return this.entityManager.createQuery(this.query_roots_jpql, idType)
                .setFlushMode(FlushModeType.AUTO)
                .getResultStream();
    }
}
