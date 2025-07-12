package cn.procsl.ping.boot.jpa.support.query.repository;

import java.util.List;

public interface FromObject extends Clause {


    String baseFormClause();

    String alias();

    List<JoinObject> joins();

    default String toClauseString(BuilderContext context) {
        String def = baseFormClause() + " as " + alias();
        // 如果无join关系,则直接返回
        if (this.joins().isEmpty()) {
            return def;
        }

        // 如果有join关系
        StringBuilder builder = new StringBuilder(def);
        for (JoinObject join : joins()) {
            builder.append(context.getDelimiter());
            builder.append(join.buildJoinClauseString(context, alias()));
        }
        return builder.toString();
    }

}
