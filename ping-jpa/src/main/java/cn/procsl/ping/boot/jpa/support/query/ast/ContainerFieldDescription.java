package cn.procsl.ping.boot.jpa.support.query.ast;

import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
final class ContainerFieldDescription implements FieldDescription {

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
        // TODO
        return List.of();
    }

    @Override
    public List<Annotation> getAnnotations() {
        return ClassUtils.getAnnotations(this.field, this.getter);
    }
}
