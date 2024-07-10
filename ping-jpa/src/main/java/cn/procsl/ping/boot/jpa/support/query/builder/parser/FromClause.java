package cn.procsl.ping.boot.jpa.support.query.builder.parser;

import java.lang.reflect.AnnotatedElement;

final class FromClause extends MarkedAnnotationClause {

    public FromClause(AnnotatedElement annotatedElement) {
        super(annotatedElement);
    }

    @Override
    public String toClauseString() {
        return "";
    }

}
