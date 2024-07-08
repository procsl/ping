package cn.procsl.ping.boot.jpa.support.query.builder.parse;

import java.lang.reflect.Field;

class FieldSelectClause extends StringSelectClause {

    final Field field;

    public FieldSelectClause(int index, String name, Field field) {
        super(index, name, field.getDeclaringClass());
        this.field = field;
    }
}
