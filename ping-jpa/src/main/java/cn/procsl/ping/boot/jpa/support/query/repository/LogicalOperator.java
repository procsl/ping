package cn.procsl.ping.boot.jpa.support.query.repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public interface LogicalOperator extends Operator {

    static Operator and(Operator... operators) {
        return getOperator(" and ", operators);
    }

    static Operator and(Collection<Operator> operators) {
        if (operators == null || operators.isEmpty()) {
            return null;
        }
        return getOperator(" and ", operators.toArray(value -> new Operator[0]));
    }

    private static Operator getOperator(String dec, Operator... operators) {
        if (operators == null || operators.length == 0) {
            return Operator.NONE;
        }

        if (operators.length == 1) {
            return operators[0];
        }

        return new SimpleOperator((bc) -> Arrays.stream(operators).map(item -> item.toClauseString(bc)).filter(item -> item != null && !item.isEmpty()).collect(Collectors.joining(dec)));
    }


    static Operator or(Operator... operators) {
        return getOperator(" or ", operators);
    }

    static Operator or(Collection<Operator> operators) {
        if (operators == null || operators.isEmpty()) {
            return null;
        }
        return getOperator(" or ", operators.toArray(value -> new Operator[0]));
    }


}
