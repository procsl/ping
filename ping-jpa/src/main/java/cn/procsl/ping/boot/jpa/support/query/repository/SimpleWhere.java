package cn.procsl.ping.boot.jpa.support.query.repository;

import lombok.Builder;

import java.util.*;

public final class SimpleWhere implements WhereObject {

    @Builder
    SimpleWhere() {
    }

    private final HashSet<Parameter> hashSet = new HashSet<>();


    private List<Operator> operator = new ArrayList<>();

    public void addParameter(Parameter... parameters) {
        hashSet.addAll(Arrays.asList(parameters));
    }

    public void addParameter(Collection<Parameter> parameters) {
        hashSet.addAll(parameters);
    }

    public void addOperator(Operator... operators) {
        this.operator.addAll(Arrays.asList(operators));
    }

    public void addOperator(Collection<Operator> operators) {
        this.operator.addAll(operators);
    }

    @Override
    public Operator getOperator() {
        return LogicalOperator.and(operator);
    }

    @Override
    public Set<Parameter> getArguments() {
        return hashSet;
    }

}
