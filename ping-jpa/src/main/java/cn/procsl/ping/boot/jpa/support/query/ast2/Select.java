package cn.procsl.ping.boot.jpa.support.query.ast2;

import jakarta.annotation.Nonnull;

/**
 * 查询表达式结构
 */
public interface Select extends Node {

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
        return "%s as %s".formatted(this.getExpression().toExpString(), this.getAliasName());
    }
}
