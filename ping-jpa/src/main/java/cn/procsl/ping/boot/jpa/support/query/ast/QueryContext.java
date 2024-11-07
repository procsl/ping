package cn.procsl.ping.boot.jpa.support.query.ast;

public interface QueryContext {

    Object getQueryInstance();

    Class<?> getQueryClass();

    Class<?> getMapping();

}
