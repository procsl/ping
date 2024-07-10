package cn.procsl.ping.boot.jpa.support.query.builder;

public interface Clause {

    String toClauseString();

    default int order() {
        return 0;
    }

}
