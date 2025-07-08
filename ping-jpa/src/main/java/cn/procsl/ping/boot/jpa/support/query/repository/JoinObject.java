package cn.procsl.ping.boot.jpa.support.query.repository;

import lombok.NonNull;

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

    // 被关联的from对象字段
    @NonNull
    String targetJoinFieldName();

    // TODO 通过参数传进来
    default String toClauseString(String context) {
        // from如果包含join语句,在这里应该就要返回包含了join的语句表达式
        return null;
    }
}
