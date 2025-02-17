package cn.procsl.ping.boot.jpa.support.query.ast.jpa;

import cn.procsl.ping.boot.jpa.support.query.Projection;
import cn.procsl.ping.boot.jpa.support.query.ReferenceBy;
import cn.procsl.ping.boot.jpa.support.query.Select;
import cn.procsl.ping.boot.jpa.support.query.ast.*;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;

final class SelectFieldExpression implements SelectExpression {

    private final Field field;
    private final Select annotation;
    private final Projection projection;

    public SelectFieldExpression(@NonNull Field field, @NonNull Projection projection) {
        this.field = field;
        this.annotation = AnnotationUtils.findAnnotation(field, Select.class);
        this.projection = projection;
    }

    /**
     * 是否是select查询字段
     */
    public boolean isSelectNode() {
        return this.annotation != null;
    }

    public boolean isRequired() {
        if (isSelectNode()) {
            return this.annotation.required();
        }
        return false;
    }

    @Nonnull
    @Override
    public String getAliasName() {
        preCheck();

        String alias = this.annotation.alias();
        if (alias == null || alias.isEmpty()) {
            return this.field.getName();
        }
        return alias;

    }

    private void preCheck() {
        if (this.isSelectNode()) {
            return;
        }
        throw new IllegalStateException("未标注@SelectField注解: "
            + this.field.getDeclaringClass() + "." + this.field.getName());
    }

    @Nonnull
    @Override
    public Expression getExpression() {

        preCheck();

        String exp = this.annotation.expression();
        if (exp != null && !exp.isEmpty()) {
            return new WrapperExpression(new StringExpression(exp));
        }

        ReferenceBy ref = AnnotationUtils.findAnnotation(this.field, ReferenceBy.class);
        if (ref == null) {
            return new DotExpression(this.projection.alias(), this.field.getName());
        }

        String name;
        if (ref.target() == null || ref.target().isEmpty()) {
            name = this.field.getName();
        } else {
            name = ref.target();
        }
        return new DotExpression(ref.ref(), name);

    }

    @Override
    public String toString() {
        return this.toExpString();
    }
}
