package cn.procsl.ping.boot.jpa.support.query.ast;

import jakarta.persistence.criteria.JoinType;

public interface FromClause extends Clause, Order {

    /**
     * 表/实体别名
     */
    String getTableAlias();

    /**
     * 关联其他实体/表 并返回新的表达式对象
     *
     * @param mainField   主表关联字段
     * @param targetField 目前表关联字段
     * @param joinType    关联类型
     * @param joinClause  目标表对象
     * @return 返回新的关联对象
     */
    JoinItemClause toJoinOn(JoinType joinType, FromClause joinClause, String mainField, String targetField);

    @Override
    default int getOrder() {
        return 0;
    }
}
