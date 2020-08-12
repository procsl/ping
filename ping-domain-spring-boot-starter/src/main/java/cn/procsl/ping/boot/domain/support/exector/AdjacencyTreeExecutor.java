package cn.procsl.ping.boot.domain.support.exector;

import cn.procsl.ping.boot.domain.business.entity.AdjacencyNode;
import cn.procsl.ping.boot.domain.business.entity.AdjacencyPathNode;
import cn.procsl.ping.boot.domain.support.business.AdjacencyTreeRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.ResolvableType;
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
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 邻接树数据库操作
 *
 * @author procsl
 * @date 2020/07/31
 */
@Slf4j
@NoRepositoryBean
@Transactional(readOnly = true)
class AdjacencyTreeExecutor<T extends AdjacencyNode, ID> implements AdjacencyTreeRepository<T, ID> {

    final JpaEntityInformation<T, ?> entityInformation;

    final EntityManager entityManager;

    final PersistenceProvider provider;

    final EscapeCharacter escapeCharacter;

    final @Nullable
    CrudMethodMetadata metadata;

    final Class<T> javaType;

    final Class<?> idType;

    final Class<? extends AdjacencyPathNode> pathNodeType;

    final String query;

    public AdjacencyTreeExecutor(JpaEntityInformation<T, ?> entityInformation,
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
        pathNodeType = this.findPathNodType(javaType);

        String template = "select tree from %s as tree where tree.id in (select b.pathId from %s as a inner join a.path as b where b.id =:parent_id) order by tree.depth asc";
        String name = javaType.getName();
        this.query = String.format(template, name, name);
    }

    /**
     * 查询当前节点的所有父节点
     *
     * @param id 指定的ID
     * @return 父节点Stream
     */
    @Override
    public Stream<T> parents(ID id) {
        val tmp = this.entityManager
                .createQuery(query, javaType)
                .setLockMode(LockModeType.NONE)
                .setFlushMode(FlushModeType.COMMIT)
                .setParameter("parent_id", id)
                .getResultStream();
        return tmp;
    }

    /**
     * 获取所有的子节点
     *
     * @param id 指定的id
     * @return 子节点
     */
    @Override
    public Stream<T> children(ID id) {
        log.info("call children");
        return null;
    }

    /**
     * 获取直接父节点
     *
     * @param id 指定的节点ID
     * @return
     */
    @Override
    public Optional<T> directParent(ID id) {
        log.info("directParent");
        return Optional.empty();
    }

    /**
     * 查询 PathNode 节点类型
     *
     * @param javaType 实体类型
     * @return PathNode 类型
     */
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
