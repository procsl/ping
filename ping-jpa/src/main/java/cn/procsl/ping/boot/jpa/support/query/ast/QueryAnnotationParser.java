package cn.procsl.ping.boot.jpa.support.query.ast;

import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
final class QueryAnnotationParser {

    final ProjectionFieldDescription description;


    public Collection<SelectClause> getSelectClauses() {

        List<SelectClause> selectClauseList = new ArrayList<>();
        List<Annotation> annotations = this.description.getAnnotations();
//        Projection projection = ClassUtils.createMargeAnnotation(Projection.class, annotations);
//        Class<?> entity = projection.entity();


        return selectClauseList;
    }

}
