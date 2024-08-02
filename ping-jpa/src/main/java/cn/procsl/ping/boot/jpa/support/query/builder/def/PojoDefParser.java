package cn.procsl.ping.boot.jpa.support.query.builder.def;

import cn.procsl.ping.boot.jpa.support.query.Projection;
import cn.procsl.ping.boot.jpa.support.query.Ref;
import jakarta.persistence.Entity;
import lombok.NonNull;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class PojoDefParser implements PojoDef {

    private final Field parentField;
    private final Class<?> pojoType;
    private final Class<?> stp;
    private final FieldEntityRefParser entityParser;

    private PojoDefParser(@NonNull Field parentField) {
        this.parentField = parentField;
        this.pojoType = parentField.getType();
        this.stp = parentField.getDeclaringClass();
        this.entityParser = new FieldEntityRefParser(new AnnotatedElement[]{parentField, pojoType, stp}, null);
    }

    public PojoDefParser(@NonNull Class<?> pojo) {
        this.pojoType = pojo;
        this.parentField = null;
        this.entityParser = new FieldEntityRefParser(new AnnotatedElement[]{pojoType}, null);
        this.stp = null;
    }

    @Override
    public List<FieldDef> getFields() {

        final HashSet<Class<?>> clazz = new HashSet<>();
        Field[] fields = this.pojoType.getDeclaredFields();
        List<FieldDef> names = new ArrayList<>();
        for (Field field : fields) {

            String name = field.getName();

            Class<?> type = field.getType();
            boolean simple = this.isSimpleType(type);

            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                continue;
            }

            if (simple) {
                FieldEntityRefParser ref = new FieldEntityRefParser(field, parentField);
                names.add(new SimpleDef.InnerDef(name, type, ref));
                continue;
            }

            boolean isCollection = this.isCollection(type);
            if (isCollection) {
//                names.add(new CollectionDef.InnerDef(name, type, null));
                continue;
            }

            boolean isMap = this.isMap(type);
            if (isMap) {
                names.add(new MapDef.InnerDef(name, type));
                continue;
            }
            if (clazz.contains(type)) {
                continue;
            }
            names.add(new PojoDefParser(field));
            clazz.add(type);
        }
        return names;
    }

    @Override
    public List<Annotation> getAnnotations(Class<? extends Annotation> annotation) {
        ArrayList<Annotation> list = new ArrayList<>();

        AnnotatedElement[] ele = new AnnotatedElement[]{pojoType, stp, parentField};
        for (AnnotatedElement element : ele) {
            if (element == null) {
                continue;
            }
            Annotation tmp = AnnotationUtils.findAnnotation(element, annotation);
            list.add(tmp);
        }
        return list;
    }

    @Override
    public Class<?> getEntityClass() {

        String alias = this.entityParser.getEntityAlias();

        AnnotatedElement[] ele = new AnnotatedElement[]{pojoType, stp};
        ArrayList<Projection> pro = new ArrayList<>();
        for (AnnotatedElement element : ele) {
            // 找到别名之后, 再查找类型
            if (element == null) {
                continue;
            }
            Projection projection = AnnotationUtils.findAnnotation(element, Projection.class);
            if (projection != null) {
                pro.add(projection);
            }
        }

        for (Projection projection : pro) {
            if (!alias.equals(projection.alias())) {
                continue;
            }
            Class<?> tmp = projection.entity();
            Entity entity = AnnotationUtils.findAnnotation(tmp, Entity.class);
            if (entity != null) {
                return tmp;
            }
        }

        for (Projection projection : pro) {
            Class<?> tmp = projection.entity();
            Entity entity = AnnotationUtils.findAnnotation(tmp, Entity.class);
            if (entity != null) {
                return tmp;
            }
        }

        throw new IllegalArgumentException("找不到对应的实体");
    }

    @Override
    public String getEntityAlias() {
        return this.entityParser.getEntityAlias();
    }

    public boolean isSimpleType(@NonNull Class<?> type) {
        if (type.isPrimitive()) {
            return true;
        }
        if (type.equals(String.class)) {
            return true;
        }
        if (type.equals(Integer.class)) {
            return true;
        }
        if (type.equals(Long.class)) {
            return true;
        }
        if (type.equals(Short.class)) {
            return true;
        }
        if (type.equals(Character.class)) {
            return true;
        }
        if (type.isAssignableFrom(Date.class)) {
            return true;
        }
        return false;
    }

    public boolean isCollection(Class<?> type) {
        if (type.isAssignableFrom(Collection.class)) {
            return true;
        }
        return false;
    }

    public boolean isMap(Class<?> type) {
        if (type.isAssignableFrom(Map.class)) {
            return true;
        }
        return false;
    }


    @Override
    public Class<?> getType() {
        return pojoType;
    }

    @Override
    public String getFieldName() {
        if (parentField != null) {
            return parentField.getName();
        }
        return null;
    }

    static class FieldEntityRefParser implements EntityRef {


        final AnnotatedElement[] item;
        private final String name;


        public FieldEntityRefParser(Field field, Field parentField) {
            Class<?> fieldType = field.getType();
            Class<?> declaringClass = field.getDeclaringClass();
            this.name = field.getName();
            this.item = new AnnotatedElement[]{parentField, fieldType, declaringClass, field};
        }

        public FieldEntityRefParser(AnnotatedElement[] item, String name) {
            this.item = item;
            this.name = name;
        }

        @Override
        public String getEntityFieldName() {
            return this.name;
        }


        @Override
        public String getEntityAlias() {

            for (AnnotatedElement element : item) {
                String alias = getAliasByRef(element);
                if (alias != null && !alias.isEmpty()) {
                    return alias;
                }
            }

            for (AnnotatedElement element : item) {
                String alias = getAliasByProjection(element);
                if (alias != null && !alias.isEmpty()) {
                    return alias;
                }
            }
            throw new IllegalArgumentException("找不到别名: " + name);
        }

        private String getAliasByRef(AnnotatedElement annotatedElement) {
            if (annotatedElement == null) {
                return null;
            }
            Ref ref = AnnotationUtils.findAnnotation(annotatedElement, Ref.class);
            if (ref == null) {
                return null;
            }
            return ref.value();
        }

        private String getAliasByProjection(AnnotatedElement annotatedElement) {
            if (annotatedElement == null) {
                return null;
            }
            Projection projection = AnnotationUtils.findAnnotation(annotatedElement, Projection.class);
            if (projection == null) {
                return null;
            }
            return projection.alias();
        }
    }

}
