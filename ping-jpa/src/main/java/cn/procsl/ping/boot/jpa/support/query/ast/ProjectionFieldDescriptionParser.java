package cn.procsl.ping.boot.jpa.support.query.ast;

import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

public class ProjectionFieldDescriptionParser implements FieldDescription {

    private final String fieldName;
    private final Class<?> projectionClass;
    private final HashSet<String> processed;
    private final boolean finalNode;

    public ProjectionFieldDescriptionParser(String fieldName, Class<?> projectionClass, HashSet<String> processed) {
        this.fieldName = fieldName;
        this.projectionClass = projectionClass;
        this.processed = processed;
        this.finalNode = this.processed.contains(this.projectionClass.getName());
        this.processed.add(this.projectionClass.getName());
    }

    public ProjectionFieldDescriptionParser(String fieldName, Class<?> projectionClass) {
        this(fieldName, projectionClass, new HashSet<>());
    }

    @Setter
    private Field field;

    @Setter
    private Method getter;

    @Setter
    private FieldDescription parent;

    @Override
    public Optional<FieldDescription> getParentField() {
        return parent != null ? Optional.of(parent) : Optional.empty();
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public Class<?> getType() {
        return projectionClass;
    }

    @Override
    public List<FieldDescription> getChildren() {
        if (this.finalNode) {
            return Collections.emptyList();
        }

        List<Field> items = ClassUtils.extractFields(this.projectionClass);
        HashMap<String, Field> fields = new HashMap<>();
        items.forEach(item -> fields.put(item.getName(), item));

        HashMap<String, Method> methodsMap = new HashMap<>();
        List<Method> methods = ClassUtils.extractGetAndIsMethods(this.projectionClass);

        for (Method method : methods) {
            String name = ClassUtils.extractMethodName(method);
            if (name != null) {
                methodsMap.put(name, method);
            }
        }
        HashSet<String> names = new HashSet<>();
        names.addAll(fields.keySet());
        names.addAll(methodsMap.keySet());
        Function<String, FieldDescription> creator = item -> new ComposeFieldDescription(item, fields.get(item), methodsMap.get(item), this, this.processed);
        return names.stream().map(creator).toList();
    }

    @Override
    public List<Annotation> getAnnotations() {
        return ClassUtils.getAnnotations(this.projectionClass, this.field, this.getter);
    }
}
