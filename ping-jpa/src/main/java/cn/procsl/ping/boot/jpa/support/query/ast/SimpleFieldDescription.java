package cn.procsl.ping.boot.jpa.support.query.ast;

import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
final class SimpleFieldDescription implements FieldDescription {

    final String name;
    final Field field;
    final Method getter;
    final Class<?> mainType;
    final FieldDescription parent;


    @Override
    public Optional<FieldDescription> getParentField() {
        return Optional.ofNullable(parent);
    }

    @Override
    public String getFieldName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return mainType;
    }

    @Override
    public List<FieldDescription> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public List<Annotation> getAnnotations() {
        return ClassUtils.getAnnotations(this.field, this.getter);
    }
}
