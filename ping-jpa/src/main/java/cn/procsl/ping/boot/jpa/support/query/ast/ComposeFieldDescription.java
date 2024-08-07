package cn.procsl.ping.boot.jpa.support.query.ast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

class ComposeFieldDescription implements FieldDescription {

    final FieldDescription compose;

    public ComposeFieldDescription(String name, Field field, Method getter, FieldDescription parent, HashSet<String> processed) {

        Class<?> mainType = null;
        if (getter != null) {
            mainType = getter.getReturnType();
        }

        if (field != null) {
            mainType = field.getType();
        }

        if (mainType == null) {
            throw new RuntimeException(name + " is null");
        }

        boolean simple = ClassUtils.isSimpleType(mainType);

        if (simple) {
            this.compose = new SimpleFieldDescription(name, field, getter, mainType, parent);
            return;
        }

        boolean container = ClassUtils.isContainerType(mainType);

        if (container) {
            this.compose = new ContainerFieldDescription(name, field, getter, mainType, parent);
            return;
        }
        ProjectionFieldDescriptionParser tmp = new ProjectionFieldDescriptionParser(name, mainType, processed);
        tmp.setField(field);
        tmp.setGetter(getter);
        tmp.setParent(parent);
        this.compose = tmp;
    }

    @Override
    public Optional<FieldDescription> getParentField() {
        return compose.getParentField();
    }

    @Override
    public String getFieldName() {
        return compose.getFieldName();
    }

    @Override
    public Class<?> getType() {
        return compose.getType();
    }

    @Override
    public List<FieldDescription> getChildren() {
        return compose.getChildren();
    }

    @Override
    public List<Annotation> getAnnotations() {
        return compose.getAnnotations();
    }

}
