package cn.procsl.ping.boot.jpa.support.query.repository;

import cn.procsl.ping.boot.jpa.support.query.From;
import cn.procsl.ping.boot.jpa.support.query.Predicate;

import java.util.List;


public interface PredicateMeta {

    enum Source {
        type, method, field
    }

    enum TypeCategory {
        collection, simple, string, map, complex
    }

    boolean isRoot();

    Predicate getPredicate();

    List<Parameter> extractParameterPlaceholderAndValue();

    Operator createOperator(From from);

    boolean shouldIgnore();

    boolean ignoreIfBlank();
}
