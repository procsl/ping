package cn.procsl.ping.boot.domain.support.executor;

import cn.procsl.ping.boot.domain.business.state.model.BooleanStateful;
import cn.procsl.ping.boot.domain.business.state.repository.BooleanStatefulRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.Serializable;

@Slf4j
@NoRepositoryBean
class BooleanStatefulExecutor<T extends BooleanStateful, ID extends Serializable>
    implements BooleanStatefulRepository<T, ID> {

    private final EntityManager entityManager;
    private final EscapeCharacter escapeCharacter;
    private final CrudMethodMetadata metadata;
    private final JPAQueryFactory jpaQueryFactory;
    private final JpaEntityInformation<T, ID> entityInformation;
    private final Class<T> javaType;
    private final Class<ID> idType;

    private final String UPDATE_JPQL;
    private final String STATE_JPQL;

    public BooleanStatefulExecutor(JpaEntityInformation<T, ID> entityInformation,
                                   EntityManager entityManager,
                                   EscapeCharacter escapeCharacter,
                                   CrudMethodMetadata metadata,
                                   BeanFactory beanFactory
    ) {

        this.entityManager = entityManager;
        this.escapeCharacter = escapeCharacter;
        this.metadata = metadata;
        this.jpaQueryFactory = beanFactory.getBean(JPAQueryFactory.class);
        this.entityInformation = entityInformation;
        this.javaType = entityInformation.getJavaType();
        this.idType = entityInformation.getIdType();
        UPDATE_JPQL = String.format("update %s as state_table set state_table.state=:state where state_table.id=:id", javaType.getName());

        STATE_JPQL = String.format("select state_table.state from %s as state_table where state_table.id=:id", javaType.getName());
    }


    /**
     * 禁用指定的实体
     *
     * @param id
     */
    @Override
    @Transactional
    public int disable(@NonNull ID id) {
        return this.changeState(id, BooleanStateful.DISABLE_STATE);
    }

    /**
     * 启用指定的实体
     *
     * @param id
     */
    @Override
    @Transactional
    public int enable(@NonNull ID id) {
        return this.changeState(id, BooleanStateful.ENABLE_STATE);
    }

    /**
     * 修改状态
     *
     * @param id    指定的ID
     * @param state 状态
     */
    @Override
    @Transactional
    public int changeState(@NonNull ID id, @NonNull Boolean state) {
        int tmp = entityManager
            .createQuery(this.UPDATE_JPQL)
            .setParameter("id", id)
            .setParameter("state", state)
            .executeUpdate();
        if (tmp <= 0) {
            return tmp;
        }
        T reference = entityManager.getReference(javaType, id);
        entityManager.detach(reference);
        return tmp;
    }

    /**
     * 获取指定实体的状态
     *
     * @param id 指定的实体
     * @return 返回实体状态
     */
    @Override
    @Transactional
    public Boolean getState(@NonNull ID id) {
        // TODO
        return null;
    }
}
