package cn.procsl.ping.boot.jpa.support.query.repository;

public interface Clause {

    String toClauseString(BuilderContext context);

}
