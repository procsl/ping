package cn.procsl.ping.boot.jpa.support.query.ast;

/**
 * 查询字段的定义
 */
public interface SelectClause extends Clause, Order {

    /**
     * 查询字段别名
     */
    String getSelectFieldAlias();

    /**
     * 列查询语句
     */
    String getQueryColumnClause();

    /**
     * 查询字段映射类型
     */
    Class<?> getFieldMappingType();

    @Override
    default ClauseType getClauseType() {
        return ClauseType.select;
    }
}
