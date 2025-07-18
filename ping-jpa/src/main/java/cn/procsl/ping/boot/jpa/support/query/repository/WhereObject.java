package cn.procsl.ping.boot.jpa.support.query.repository;

import java.util.Set;

public interface WhereObject extends Clause {

    Set<Parameter> getArguments();

    Operator getOperator();

    @Override
    default String toClauseString(BuilderContext context) {
        return this.getOperator().toClauseString(context);
    }
}
