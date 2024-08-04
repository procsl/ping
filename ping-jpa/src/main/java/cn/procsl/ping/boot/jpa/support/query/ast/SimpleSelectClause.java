package cn.procsl.ping.boot.jpa.support.query.ast;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class SimpleSelectClause implements SelectClause {

    final private Clause subClause;

    @Getter
    final private String selectFieldAlias;

    @Getter
    final private Class<?> fieldMappingType;

    @Getter
    final private int order;

    @Override
    public String toClauseString() {
        String sub = subClause == null ? "" : subClause.toClauseString();
        return "%s as %s".formatted(sub, selectFieldAlias);
    }

    @Override
    public String toString() {
        return this.toClauseString();
    }

}
