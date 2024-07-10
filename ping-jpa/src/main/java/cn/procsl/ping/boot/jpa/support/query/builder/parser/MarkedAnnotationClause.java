package cn.procsl.ping.boot.jpa.support.query.builder.parser;

import cn.procsl.ping.boot.jpa.support.query.builder.Clause;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.AnnotatedElement;

@RequiredArgsConstructor
abstract class MarkedAnnotationClause implements Clause {

    final AnnotatedElement marked;

}
