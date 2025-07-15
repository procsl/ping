package cn.procsl.ping.boot.jpa.support.query.repository;

import java.util.function.Function;

public record SimpleOperator(Function<BuilderContext, String> op) implements Operator {
    @Override
    public String toClauseString(BuilderContext context) {
        return op.apply(context);
    }

}
