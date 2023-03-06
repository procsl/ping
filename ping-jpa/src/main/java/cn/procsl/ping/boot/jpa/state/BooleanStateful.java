package cn.procsl.ping.boot.jpa.state;

import java.beans.Transient;

/**
 * bool类型状态化接口
 */
public interface BooleanStateful extends Stateful<Boolean> {

    boolean ENABLE_STATE = true;

    boolean DISABLE_STATE = !ENABLE_STATE;

    void setState(Boolean state);

    default void disable() {
        setState(!ENABLE_STATE);
    }

    default void enable() {
        setState(ENABLE_STATE);
    }

    @Transient
    boolean isEnable();

}
