package cn.procsl.ping.boot.jpa.support.query.builder;

import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
public class QueryBuilderParseAdapter implements QueryStringBuilder {


    @Override
    public String buildQueryString(QueryClauseParser parse) {
        final SimpleQueryStringBuilder builder = new SimpleQueryStringBuilder();

        List<SelectClause> selects = parse.parseSelects();
        List<Clause> from = parse.parseFrom();
        List<Clause> where = parse.parseWhere();
        List<Clause> group = parse.parseGroupBy();
        List<Clause> order = parse.parseOrderBy();

        selects.sort(Comparator.comparingInt(Clause::order));
        from.sort(Comparator.comparingInt(Clause::order));
        where.sort(Comparator.comparingInt(Clause::order));
        group.sort(Comparator.comparingInt(Clause::order));
        order.sort(Comparator.comparingInt(Clause::order));

        selects.forEach(builder::addSelect);
        from.forEach(builder::addFrom);
        where.forEach(builder::addWhere);
        group.forEach(builder::addGroupBy);
        order.forEach(builder::addOrderBy);
        return builder.buildQueryString(parse);
    }
}
