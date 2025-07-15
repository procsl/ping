package cn.procsl.ping.boot.jpa.support.query.repository;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class SimpleWhere implements WhereObject {

    public SimpleWhere(Operator operator, Argument... argument) {
        this.operator = operator;
        if (argument != null) {
            this.hashSet.addAll(Arrays.asList(argument));
        }
    }

    private final HashSet<Argument> hashSet = new HashSet<>();

    @Setter
    @Getter
    private Operator operator;


    @Override
    public Set<Argument> getArguments() {
        return hashSet;
    }

}
