package cn.procsl.ping.boot.jpa.support.query.repository;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class SimpleWhere implements WhereObject {

    public SimpleWhere(Operator operator, Parameter... parameter) {
        this.operator = operator;
        if (parameter != null) {
            this.hashSet.addAll(Arrays.asList(parameter));
        }
    }

    private final HashSet<Parameter> hashSet = new HashSet<>();

    @Setter
    @Getter
    private Operator operator;


    @Override
    public Set<Parameter> getArguments() {
        return hashSet;
    }

}
