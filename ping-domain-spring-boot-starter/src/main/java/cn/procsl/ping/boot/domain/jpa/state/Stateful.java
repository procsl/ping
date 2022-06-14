package cn.procsl.ping.boot.domain.jpa.state;


import java.io.Serializable;

/**
 * 状态化接口
 *
 * @param <S> 状态类型
 */
public interface Stateful<S extends Serializable> {

    S getState();

}
