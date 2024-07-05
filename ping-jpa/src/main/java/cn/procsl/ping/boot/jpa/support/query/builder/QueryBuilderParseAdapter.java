package cn.procsl.ping.boot.jpa.support.query.builder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QueryBuilderParseAdapter implements QueryStringBuilder {

    final QueryClauseParse parse;

    @Override
    public String buildQueryString() {
        final SimpleQueryStringBuilder builder = new SimpleQueryStringBuilder();
        parse.parseSelects().forEach(builder::addSelect);
        parse.parseFrom().forEach(builder::addFrom);
        parse.parseWhere().forEach(builder::addWhere);
        parse.parseGroupBy().forEach(builder::addGroupBy);
        parse.parseOrderBy().forEach(builder::addOrderBy);
        return builder.buildQueryString();
    }
}
