package cn.procsl.ping.boot.jpa.support.query.repository;

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

    static Operator not_in(String field, String args) {
        return (new TemplateOperator("%s not in ( %s )", field, args));
    }

    static Operator between(String field, String arg1, String arg2) {
        return (new TemplateOperator("%s between %s and %s", field, arg1, arg2));
    }

    static Operator is_not_null(String field) {
        return (new TemplateOperator("%s is not null", field));
    }

    static Operator is_null(String field) {
        return (new TemplateOperator("%s is null", field));
    }

    static Operator custom(String named) {
        return new SimpleOperator((bc) -> named);
    }
}
