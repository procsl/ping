package cn.procsl.ping.boot.domain.state;


/**
 * 状态化接口
 *
 * @param <ID> 实体ID
 * @param <S>  状态类型
 */
public interface Stateful<ID, S> {
    S getState();

    void setState(S state);
}
