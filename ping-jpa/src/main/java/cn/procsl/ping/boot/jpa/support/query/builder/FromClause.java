package cn.procsl.ping.boot.jpa.support.query.builder;

public interface FromClause extends Alias, Clause {

    void addJoinClause(JoinClause joinClause);

}
