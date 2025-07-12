package cn.procsl.ping.boot.jpa.support.query.repository;

import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

public interface JoinObject {

    enum JOIN_TYPE {
        inner, left, right
    }

    // join类型
    @NonNull
    JOIN_TYPE getJoinType();

    // 被join的from对象
    @NonNull
    FromObject targetFromObject();

    @NonNull
    String currentJoinFieldName();

    // 被关联的from对象字段
    @NonNull
    String targetJoinFieldName();

    // Table_A as a inner join Table_B as b on a.id = b.id
    // inner join Table_C as c on b.id = c.id
    // 如果 Table_C 为 无join关系表
    // 1. 返回  inner join Table_C as c on b.id = c.id
    // 如果 Table_B 为有join关系的表
    // 2. 返回  inner join Table_B as b on a.id = b.id inner join Table_C as c on b.id = c.id
    default String buildJoinClauseString(BuilderContext context, String tableAlias) {

        // 获取被join的from的语句
        String formStr = this.targetFromObject().toClauseString(context);

        // equal 表达式
        String equal = "%s.%s = %s.%s".formatted(tableAlias, this.currentJoinFieldName(), this.targetFromObject().alias(), this.targetJoinFieldName());

        return "%s join %s on %s".formatted(this.getJoinType(), formStr, equal);
    }
}
