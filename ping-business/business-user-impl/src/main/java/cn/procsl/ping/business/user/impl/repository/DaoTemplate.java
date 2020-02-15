package cn.procsl.ping.business.user.impl.repository;

import cn.procsl.ping.business.user.impl.entity.EntityObject;

import java.io.Serializable;

/**
 * dao实体操作类
 *
 * @author procsl
 * @date 2019/12/14
 */
public interface DaoTemplate<T extends Serializable, Entity extends EntityObject<T>> {

    /**
     * 保存
     *
     * @param entities
     * @return
     */
    void save(Entity entities);

    /**
     * 更新
     *
     * @param entities 实体列表
     * @return
     */
    void update(Entity entities);

    /**
     * 实体列表
     *
     * @param entities
     * @return
     */
    void delete(Entity entities);

    /**
     * 加载实体
     *
     * @param id
     * @return
     */
    Entity load(T id);
}
