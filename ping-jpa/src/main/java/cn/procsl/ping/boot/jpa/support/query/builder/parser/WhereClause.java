package cn.procsl.ping.boot.jpa.support.query.builder.parser;

import lombok.Getter;

import java.lang.reflect.AnnotatedElement;

@Getter
final class WhereClause extends MarkedAnnotationClause {


    public WhereClause(AnnotatedElement marked) {
        super(marked);
    }

    @Override
    public String toClauseString() {
        return "";
    }

}
