package cn.procsl.ping.boot.jpa.support.query.ast2.jpa;

import cn.procsl.ping.boot.jpa.support.query.ast2.Expression;
import cn.procsl.ping.boot.jpa.support.query.ast2.Select;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;

@RequiredArgsConstructor
class SelectField implements Select {

    final Field field;

    @Nonnull
    @Override
    public String getAliasName() {
        return null;
    }

    @Nonnull
    @Override
    public Expression getExpression() {
        return null;
    }

}
