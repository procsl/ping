package cn.procsl.ping.boot.jpa.support.query.repository;

public interface BuilderContext {

    default String getDelimiter() {
        return "\n";
    }

    default boolean isLowCase() {
        return false;
    }

    default boolean isFormat() {
        return true;
    }

}
