package cn.procsl.ping.boot.domain.support.exector;

import cn.procsl.ping.boot.domain.business.entity.AdjacencyNode;
import cn.procsl.ping.boot.domain.support.business.AdjacencyTreeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
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

    private final JpaEntityInformation<T, ?> entityInformation;

    private final EntityManager em;

    private final PersistenceProvider provider;

    private final EscapeCharacter escapeCharacter;

    private final @Nullable
    CrudMethodMetadata metadata;

    public AdjacencyTreeExecutor(JpaEntityInformation<T, ?> entityInformation,
                                 EntityManager em,
                                 EscapeCharacter escapeCharacter,
                                 @Nullable CrudMethodMetadata metadata) {
        this.entityInformation = entityInformation;
        this.em = em;
        this.escapeCharacter = escapeCharacter;
        this.metadata = metadata;
        this.provider = PersistenceProvider.fromEntityManager(em);
    }

    /**
     * 查询当前节点的父节点
     *
     * @param id 指定的ID
     * @return 父节点Stream
     */
    @Override
    public Stream<T> parents(ID id) {
        log.info("call parents");
        return null;
    }

    @Override
    public Stream<T> children(ID id) {
        log.info("call children");
        return null;
    }

    @Override
    public Optional<T> directParent(ID id) {
        log.info("directParent");
        return Optional.empty();
    }
}
