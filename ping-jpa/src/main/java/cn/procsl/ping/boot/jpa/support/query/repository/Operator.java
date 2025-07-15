package cn.procsl.ping.boot.jpa.support.query.repository;

import java.util.Arrays;
import java.util.stream.Collectors;

public interface Operator extends Clause {

    Operator NONE = new TemplateOperator("");

    static Operator eq(String field, String args) {
        return (new TemplateOperator("%s = %s", field, args));
    }

    static Operator group(Operator value) {
        return new SimpleOperator((bc) -> "( %s )".formatted(value.toClauseString(bc)));
    }


    static Operator like(String field, String args) {
        return (new TemplateOperator("%s like %s", field, args));
    }

    static Operator notLike(String field, String args) {
        return (new TemplateOperator("%s not like %s", field, args));
    }

    static Operator ne(String field, String args) {
        return (new TemplateOperator("%s != %s", field, args));
    }

    static Operator gt(String field, String args) {
        return (new TemplateOperator("%s > %s", field, args));
    }

    static Operator lt(String field, String args) {
        return (new TemplateOperator("%s < %s", field, args));
    }

    static Operator gte(String field, String args) {
        return (new TemplateOperator("%s >= %s", field, args));
    }

    static Operator lte(String field, String args) {
        return (new TemplateOperator("%s <= %s", field, args));
    }

    static Operator in(String field, String args) {
        return (new TemplateOperator("%s in ( %s )", field, args));
    }

    static Operator notIn(String field, String args) {
        return (new TemplateOperator("%s not in ( %s )", field, args));
    }

    static Operator between(String field, String arg1, String arg2) {
        return (new TemplateOperator("%s between %s and %s", field, arg1, arg2));
    }


}
