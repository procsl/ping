package cn.procsl.ping.boot.jpa.support.query.repository;

public interface WhereExpression extends Clause {

    // 左右形式的表达式 :a > :b  < = >= <= !=
    // between :a and :b
    default WhereExpression eq(String field, String args) {
        return new TemplateExpression("%s = %s", field, args);
    }

    default WhereExpression ne(String field, String args) {
        return new TemplateExpression("%s != %s", field, args);
    }

    default WhereExpression gt(String field, String args) {
        return new TemplateExpression("%s > %s", field, args);
    }

    default WhereExpression lt(String field, String args) {
        return new TemplateExpression("%s < %s", field, args);
    }

    default WhereExpression gte(String field, String args) {
        return new TemplateExpression("%s >= %s", field, args);
    }

    default WhereExpression lte(String field, String args) {
        return new TemplateExpression("%s <= %s", field, args);
    }

    default WhereExpression between(String field, String arg1, String arg2) {
        return new TemplateExpression("%s between %s and %s", field, arg1, arg2);
    }

    default WhereExpression and(WhereExpression left, WhereExpression right) {
        return new TemplateExpression("%s and %s", left, right);
    }


    default WhereExpression or(WhereExpression left, WhereExpression right) {
        return new TemplateExpression("(%s or %s)", left, right);
    }

}
