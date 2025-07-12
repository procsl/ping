package cn.procsl.ping.boot.jpa.support.query.repository;

public interface BuilderContext {

    default String getDelimiter() {
        return " ";
    }

}
