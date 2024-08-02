package cn.procsl.ping.boot.jpa.support.query.builder;

import java.util.List;

public interface QueryClauseParser {

    List<SelectClause> parseSelects();

    List<Clause> parseWhere();

    List<Clause> parseOrderBy();

    List<Clause> parseGroupBy();

    List<FromClause> parseFrom();
}
