package cn.procsl.ping.boot.jpa.support.query.ast;

public interface FromClause extends Clause, Order {

    /**
     * 表/实体别名
     */
    String getTableAlias();

    /**
     * from查询语句
     */
//    String getFromClause();
    @Override
    default int getOrder() {
        return 0;
    }

}
