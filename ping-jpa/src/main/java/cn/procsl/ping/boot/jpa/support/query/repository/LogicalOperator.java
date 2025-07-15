package cn.procsl.ping.boot.jpa.support.query.repository;

import java.util.Arrays;
import java.util.stream.Collectors;

public interface LogicalOperator extends Operator {

    static Operator and(Operator... operators) {
        return getOperator(" and ", operators);
    }


    private static Operator getOperator(String dec, Operator... operators) {
        if (operators == null || operators.length == 0) {
            return Operator.NONE;
        }

        if (operators.length == 1) {
            return operators[0];
        }

        return new SimpleOperator((bc) -> Arrays.stream(operators).map(item -> item.toClauseString(bc)).collect(Collectors.joining(dec)));
    }


    static Operator or(Operator... operators) {
        return getOperator(" or ", operators);
    }


}
