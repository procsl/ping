package cn.procsl.ping.boot.jpa.support.query.ast;

import jakarta.annotation.Nonnull;

/**
 * 查询表达式结构
 */
public interface SelectExpression extends Expression {

    /**
     * 返回查询字段 as 的别名
     */
    @Nonnull
    String getAliasName();

    /**
     * 获取查询表达式
     */
    @Nonnull
    Expression getExpression();

    @Override
    default String toExpString() {
        return new AliasExpression(this.getExpression(), this.getAliasName()).toExpString();
    }
}
