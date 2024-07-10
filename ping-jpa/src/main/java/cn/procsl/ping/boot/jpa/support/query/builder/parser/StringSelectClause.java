package cn.procsl.ping.boot.jpa.support.query.builder.parser;

import cn.procsl.ping.boot.jpa.support.query.builder.SelectClause;
import lombok.AllArgsConstructor;

@AllArgsConstructor
class StringSelectClause implements SelectClause {

    private final int index;
    private final String name;
    private final Class<?> targetClass;

    @Override
    public String toClauseString() {
        throw new UnsupportedOperationException("不支持的生成表达式");
    }


}
