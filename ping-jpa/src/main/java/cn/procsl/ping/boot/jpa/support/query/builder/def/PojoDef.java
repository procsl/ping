package cn.procsl.ping.boot.jpa.support.query.builder.def;

import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public interface PojoDef extends FieldDef {

    List<FieldDef> getFields();

    Class<?> getEntityClass();

    String getEntityAlias();

    default List<Annotation> getAnnotations(Class<? extends Annotation> annotation) {
        return Collections.emptyList();
    }

    @Override
    default <T, R> R accept(@NonNull FieldDefVisitor<R, T> visitor, T args) {
        return visitor.visitPojoField(this, args);
    }

}
