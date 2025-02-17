package cn.procsl.ping.boot.jpa.support.query.ast.jpa;

import cn.procsl.ping.boot.jpa.support.query.Where;
import cn.procsl.ping.boot.jpa.support.query.ast.Expression;
import cn.procsl.ping.boot.jpa.support.query.ast.WhereExpression;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;

@RequiredArgsConstructor
class WhereFieldExpression implements WhereExpression {

    final Field field;
    final Where where;

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public String groupName() {
        return null;
    }

    @Override
    public boolean isInclude() {
        return where != null;
    }

    @Override
    public String getParmaName() {
        return this;
    }

    @Override
    public Expression getExpression() {
        return null;
    }

    @Override
    public String condition() {
        return null;
    }
}
