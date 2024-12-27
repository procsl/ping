package cn.procsl.ping.boot.jpa.support.query.ast;

public interface FromClause extends Clause, Order {

    /**
     * 表/实体别名
     */
    String getTableAlias();

    @Override
    default int getOrder() {
        return 0;
    }

    @Override
    default ClauseType getClauseType() {
        return ClauseType.from;
    }
}
