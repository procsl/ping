package cn.procsl.ping.boot.user.domain.common.service;

import cn.procsl.ping.boot.domain.business.utils.CollectionUtils;
import cn.procsl.ping.business.domain.DomainEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

public abstract class AbstractService<ID extends Serializable, T extends DomainEntity<ID>> {

    final protected JpaRepository<T, ID> jpaRepository;

    final protected QuerydslPredicateExecutor<T> querydslRepository;

    public AbstractService(JpaRepository<T, ID> jpaRepository, QuerydslPredicateExecutor<T> querydslRepository) {
        this.jpaRepository = jpaRepository;
        this.querydslRepository = querydslRepository;
    }

    /**
     * 获取指定的实体, id不可为null
     *
     * @param id 指定的id
     * @return 返回对应的实体
     * @throws EntityNotFoundException 如果实体为不存在
     */
    public T getOne(@NotNull ID id) throws EntityNotFoundException, IllegalArgumentException {
        return jpaRepository.getOne(id);
    }

    /**
     * 通过ID获取
     *
     * @param id id可以为null或对应的实体不存在
     * @return 返回对应的实体
     */
    public T findById(ID id) {
        if (id == null) {
            return null;
        }
        return jpaRepository.findById(id).orElse(null);
    }


    /**
     * 通过ID查找
     *
     * @param ids ids
     * @return 返回id对应的实体
     */
    public Collection<T> findByIds(Collection<ID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return jpaRepository.findAllById(ids);
    }
}
