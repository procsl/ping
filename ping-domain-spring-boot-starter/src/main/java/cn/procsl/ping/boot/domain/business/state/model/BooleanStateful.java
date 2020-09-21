package cn.procsl.ping.boot.domain.business.state.model;

import java.io.Serializable;

/**
 * bool类型状态化接口
 *
 * @param <ID> 实体ID
 */
public interface BooleanStateful<ID extends Serializable> extends Stateful<ID, Boolean> {

    boolean ENABLE_STATE = true;

    boolean DISABLE_STATE = false;


    default void disable() {
        setState(!ENABLE_STATE);
    }

    default void enable() {
        setState(ENABLE_STATE);
    }

}
