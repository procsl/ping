package cn.procsl.business.user.entity;

import java.io.Serializable;

/**
 * 实体标识接口
 *
 * @author procsl
 * @date 2019/12/13
 */
public interface EntityObject<T extends Serializable> {
    /**
     * 获取实体ID
     *
     * @return 实体
     */
    T getId();
}
