package cn.procsl.ping.boot.jpa.support.query.ast;

import java.util.List;

public interface SelectMapper<Q> {

    List<SelectExpression> parseSelectExpression(List<WhereExpression> whereExpressions);

}
