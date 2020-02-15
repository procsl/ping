package cn.procsl.ping.business.user.impl.entity;

import java.io.Serializable;

/**
 * 实体标识接口
 *
 * @author procsl
 * @date 2019/12/13
 */
public abstract class EntityObject<T extends Serializable> {

    T id;

    /**
     * 获取实体ID
     *
     * @return 实体
     */
    public T getId() {
        return id;
    }
}
