package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.support.query.Projection;
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
        List<Projection> projections = ClassUtils.filter(annotations, Projection.class);

        return null;
    }

}
