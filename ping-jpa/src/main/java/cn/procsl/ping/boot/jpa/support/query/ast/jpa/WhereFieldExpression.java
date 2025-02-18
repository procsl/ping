package cn.procsl.ping.boot.jpa.support.query.ast.jpa;

import cn.procsl.ping.boot.jpa.support.query.Projection;
import cn.procsl.ping.boot.jpa.support.query.ReferenceBy;
import cn.procsl.ping.boot.jpa.support.query.Select;
import cn.procsl.ping.boot.jpa.support.query.Where;
import cn.procsl.ping.boot.jpa.support.query.ast.DotExpression;
import cn.procsl.ping.boot.jpa.support.query.ast.Expression;
import cn.procsl.ping.boot.jpa.support.query.ast.Variable;
import cn.procsl.ping.boot.jpa.support.query.ast.WhereExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;

@RequiredArgsConstructor
final class WhereFieldExpression implements WhereExpression {

    final Object target;
    final Field field;
    final Projection main;
    final ReferenceBy ref;
    final Where where;

    @Override
    public boolean isRequired() {
        return where != null && where.required();
    }

    @Override
    public String groupName() {
        if (where == null) {
            return null;
        }
        return where.groupName();
    }

    @Override
    public boolean isInclude() {
        return where != null;
    }

    private String getParamName() {
        Select select = AnnotationUtils.findAnnotation(this.field, Select.class);
        if (select == null || select.alias() == null || select.alias().isEmpty()) {
            return this.field.getName();
        }
        return select.alias();
    }

    @Override
    public Variable getParamVariable() {
        return new Placeholder(this.getParamName(), field, this.target);
    }

    @Override
    public Expression getExpression() {
        String name = this.field.getName();
        if (ref == null) {
            return new DotExpression(main.alias(), name);
        }
        if (ref.target() == null || ref.target().isEmpty()) {
            return new DotExpression(ref.ref(), name);
        }
        return new DotExpression(ref.ref(), ref.target());
    }

    @Override
    public String condition() {
        return this.where.condition();
    }
}
