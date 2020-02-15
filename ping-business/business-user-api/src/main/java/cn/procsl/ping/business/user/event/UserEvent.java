package cn.procsl.ping.business.user.event;

/**
 * 用户事件
 *
 * @author procsl
 * @date 2019/12/14
 */
public interface UserEvent {

    /**
     * 创建用户后
     *
     * @param id
     */
    void onCreated(String id);
}
