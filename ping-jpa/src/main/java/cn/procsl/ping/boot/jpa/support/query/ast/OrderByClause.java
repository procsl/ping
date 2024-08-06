package cn.procsl.ping.boot.jpa.support.query.ast;

public interface OrderByClause extends Clause, Order {

    String getDirection();

}
