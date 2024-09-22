package cn.procsl.ping.boot.jpa.support.query.ast;

import java.util.Collection;

public class QueryStringBuilderAdapter implements Clause {

    private final QueryAnnotationParser parser;

    private final QueryClause clause;

    public QueryStringBuilderAdapter(Class<?> pojo, QueryClause clause) {
        ProjectionFieldDescription projection = new ProjectionFieldDescription(pojo);
        this.parser = new QueryAnnotationParser(projection);
        this.clause = clause;
    }

    @Override
    public String toClauseString() {

        Collection<SelectClause> selects = this.parser.getSelectClauses();
        selects.forEach(clause::addSelectClause);


        return clause.toClauseString();
    }

}
