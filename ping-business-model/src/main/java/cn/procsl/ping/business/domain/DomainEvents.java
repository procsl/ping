package cn.procsl.ping.business.domain;

public interface DomainEvents<ID> extends DomainEntity<ID> {

    /**
     * 当新对象被加入持久化上下文时回调方法
     */
    default void postPersist() {
    }

    default void prePersist(){
    }

}
