package cn.procsl.ping.boot.domain.business.state.model;

import cn.procsl.ping.business.domain.DomainId;

/**
 * 状态化接口
 *
 * @param <ID> 实体ID
 * @param <S>  状态类型
 */
public interface Stateful<ID, S> extends DomainId<ID> {
    S getState();

    void setState(S state);
}
