package cn.procsl.ping.boot.domain.state;


import java.io.Serializable;

/**
 * 状态化接口
 *
 * @param <S> 状态类型
 */
public interface Stateful<S extends Serializable> {

    S getStatus();

    void setStatus(S status);
}
