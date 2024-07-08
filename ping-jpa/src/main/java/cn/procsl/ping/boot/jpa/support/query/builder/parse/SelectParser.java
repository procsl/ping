package cn.procsl.ping.boot.jpa.support.query.builder.parse;

import cn.procsl.ping.boot.jpa.support.query.SelectFields;
import cn.procsl.ping.boot.jpa.support.query.builder.SelectClause;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SelectParser {


    public Constructor<?> parseMarkedConstructors(Class<?> clazz) {

        Constructor<?>[] constructors = clazz.getConstructors();
        Constructor<?> markConstructor = null;
        for (Constructor<?> constructor : constructors) {
            SelectFields tmp = AnnotationUtils.findAnnotation(constructor, SelectFields.class);
            if (tmp != null && markConstructor == null) {
                markConstructor = constructor;
                continue;
            }
            if (markConstructor != null) {
                throw new IllegalArgumentException("存在多个被@InjectConstructor注解标注的构造函数: " + clazz);
            }
        }
        return markConstructor;
    }

    /**
     * 解析注解
     */
    public SelectFields parseSelectFields(Class<? extends Serializable> clazz) {
        SelectFields tmp = AnnotationUtils.findAnnotation(clazz, SelectFields.class);
        return getSelectFields(tmp);
    }

    /**
     * 暂时只处理属性
     */
    public SelectFields createDefaultSelectFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        String[] array = Arrays.stream(fields).map(Field::getName).toArray(value -> new String[0]);
        return new DefaultSelectField(array);
    }

    public List<SelectClause> parseClauses(Class<? extends Serializable> clazz, SelectFields tmp) {

        String[] ff = tmp.fields();
        ArrayList<SelectClause> arr = new ArrayList<>(ff.length);
        for (int i = 0; i < ff.length; i++) {
            String name = ff[i];
            Field field = this.getFieldByNameAndType(clazz, name, null);
            if (field != null) {
                arr.add(new FieldSelectClause(i, name, field));
                continue;
            }

            Executable executable = this.getGetterByNameAndType(clazz, name, null);
            if (executable != null) {
                arr.add(new GetterSelectClause(i, name, executable));
            }
            throw new IllegalStateException("找不到指定名称的属性或Get方法: " + name);
        }
        return arr;
    }


//    public ArrayList<StringSelectClause> parseClauses(Class<? extends Serializable> clazz, SelectFields tmp) {
//
//    }

    /**
     * 从被标记的构造函数上解析
     */
    public SelectFields parseSelectFields(Constructor<?> constructor) {
        SelectFields tmp = AnnotationUtils.findAnnotation(constructor, SelectFields.class);
        return getSelectFields(tmp);
    }

    public List<SelectClause> parseClauses(Constructor<?> constructor, SelectFields fields) {
        // 按照构造函数的index对应关系, 设置名称; 忽略构造函数上参数的名字
        // 构造函数参数名字有可能会因为编译导致丢失
        // 这里的名字以及对应的构造函数index的类型需要和实体中字段或者get方法的名字和类型相同
        String[] ff = fields.fields();
        TypeVariable<? extends Constructor<?>>[] params = constructor.getTypeParameters();
        if (ff.length != params.length) {
            throw new IllegalArgumentException("@SelectField对应的参数需要与被标记构造函数参数对应或小于构造函数参数数量");
        }

        Class<?>[] types = constructor.getParameterTypes();
        Class<?> targetType = constructor.getDeclaringClass();
        ArrayList<SelectClause> arr = new ArrayList<>(types.length);
        for (int i = 0; i < ff.length; i++) {
            String name = fields.fields()[i];

            if (name == null || name.isEmpty() || name.equals("null")) {
                arr.add(new NullSelectClause(i));
                continue;
            }

            Class<?> type = types[i];
            Field field = this.getFieldByNameAndType(targetType, name, type);
            if (field != null) {
                arr.add(new FieldSelectClause(i, name, field));
                continue;
            }

            Executable executable = this.getGetterByNameAndType(targetType, name, type);
            if (executable != null) {
                arr.add(new GetterSelectClause(i, name, executable));
            }
            throw new IllegalStateException("找不到指定名称的属性或Get方法: " + type.getName() + ", " + name);
        }

        return arr;
    }


    private Executable getGetterByNameAndType(Class<?> declaringClass, String name, Class<?> type) {
        Method[] methods = declaringClass.getMethods();
        for (Method method : methods) {
            if (!method.getName().equals(name)) {
                continue;
            }
            if (method.getParameterTypes().length != 0) {
                continue;
            }
            if (!method.getName().startsWith("get")) {
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            if (type != null) {
                if (method.getReturnType().isAssignableFrom(type)) {
                    return method;
                }
                continue;
            }
            return method;
        }
        return null;
    }

    private Field getFieldByNameAndType(Class<?> clazz, String name, Class<?> type) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (type != null) {
                if (field.getName().equals(name) && field.getType().isAssignableFrom(type)) {
                    return field;
                }
            } else if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    private SelectFields getSelectFields(SelectFields tmp) {
        if (tmp == null) {
            return null;
        }
        if (tmp.fields() == null || tmp.fields().length == 0) {
            return null;
        }
        return tmp;
    }


    @RequiredArgsConstructor
    static private class DefaultSelectField implements SelectFields {

        protected final String[] fields;

        @Override
        public Class<? extends Annotation> annotationType() {
            return SelectFields.class;
        }

        @Override
        public String[] fields() {
            return fields;
        }
    }

}
