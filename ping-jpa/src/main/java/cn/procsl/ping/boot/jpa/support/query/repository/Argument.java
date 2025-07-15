package cn.procsl.ping.boot.jpa.support.query.repository;

public interface Argument {

    String getName();

    Class<?> getType();

    // 不在生成阶段注入值
    Object getValue();

}
