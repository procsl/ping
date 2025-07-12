package cn.procsl.ping.boot.jpa.support.query.repository.expl;

import cn.procsl.ping.boot.jpa.support.query.repository.BuilderContext;
import cn.procsl.ping.boot.jpa.support.query.repository.WhereExpression;

public class TemplateExpression implements WhereExpression {

    final String template;
    final String[] args;

    @Override
    public String toClauseString(BuilderContext context) {
        return null;
    }

}
