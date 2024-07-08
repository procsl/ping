package cn.procsl.ping.boot.jpa.support.query.builder.parse;

import cn.procsl.ping.boot.jpa.support.query.builder.SelectClause;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
class StringSelectClause implements SelectClause {

    private final int index;
    private final String name;
    private final Class<?> targetClass;

    @Override
    public String toClauseString() {
        throw new UnsupportedOperationException("不支持的生成表达式");
    }




}
