package cn.procsl.ping.boot.jpa.support.query.repository;

import java.util.List;

public interface FromObject extends Clause {


    String baseFormClause();

    String alias();

    List<JoinObject> joins();

    default String toBaseClauseString(BuilderContext context) {
        return baseFormClause() + " as " + alias();
    }

    default String toClauseString(BuilderContext context) {
        String joins = this.toJoinClauseString(context);
        if (joins == null || joins.isEmpty()) {
            return this.toBaseClauseString(context);
        }

        return this.toBaseClauseString(context) + " " + joins;
    }

    default String toJoinClauseString(BuilderContext context) {
        if (this.joins() == null || this.joins().isEmpty()) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (JoinObject join : joins()) {
            if (context.isFormat()) {
                builder.append("\n\t");
            } else {
                builder.append(" ");
            }
            builder.append(join.toClauseString(context));
        }
        return builder.toString();
    }

}
