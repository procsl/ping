package cn.procsl.ping.boot.jpa.support.query.repository;

public interface Clause<C> {

    String toClauseString(C context);

}
