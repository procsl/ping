package cn.procsl.ping.boot.jpa.support.query.builder;

import cn.procsl.ping.boot.jpa.support.query.SelectFields;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class AnnotationClauseParse implements QueryClauseParse {

    final Class<? extends Serializable> clazz;

    @Override
    public List<SelectClause> parseSelects() {

        // 获取select字段
        List<StringSelectClause> fields = this.parseSelectFields();
        List<AnnotationFieldSelectClause> clause = fields.stream().map(StringSelectClause::convertToAnnotationClause).toList();

        return getStringSelectClauses(injectable, clazz);
    }


    private List<StringSelectClause> parseSelectFields() {

        // 2. 通过类上的 SelectFields 匹配类字段
        // 3. 通过构造函数参数匹配类字段
        // 4. 采用默认方式

        Constructor<?>[] constructors = this.clazz.getConstructors();
        Constructor<?> markConstructor = null;
        SelectFields selectFields = null;
        for (Constructor<?> constructor : constructors) {
            SelectFields tmp = AnnotationUtils.findAnnotation(constructor, SelectFields.class);
            if (tmp != null && markConstructor == null) {
                markConstructor = constructor;
                selectFields = tmp;
                continue;
            }
            if (markConstructor != null) {
                throw new IllegalArgumentException("存在多个被@InjectConstructor注解标注的构造函数: " + this.clazz);
            }
        }

        // 1. 通过构造函数上的 SelectFields 匹配类字段/get方法;
        if (selectFields != null && selectFields.fields().length != 0) {
            return getStringSelectClauses(selectFields, this.clazz);
        }

        // 获取类上的 selectFields
        SelectFields clazzFields = AnnotationUtils.findAnnotation(this.clazz, SelectFields.class);
        if (clazzFields != null && clazzFields.fields().length != 0) {
            return getStringSelectClauses(clazzFields, this.clazz);
        }

        // 3. 通过构造函数参数匹配类字段
        // 判断是否标注了 SelectFields, 如果被标注了, 就使用被标注的构造方法对应的字段名称
        if (selectFields != null) {
            TypeVariable<? extends Constructor<?>>[] params = markConstructor.getTypeParameters();
            Class<?>[] types = markConstructor.getParameterTypes();
            ArrayList<StringSelectClause> arr = new ArrayList<>(params.length);
            for (int i = 0; i < params.length; i++) {
                var param = params[i];
                // TODO 优先通过注解获取名字?
                String name = param.getName();
                arr.add(new AnnotationFieldSelectClause(i, name, types[i], clazz));
            }
            return arr;
        }

        // 使用默认的方式
        Field[] fields = this.clazz.getDeclaredFields();
        ArrayList<StringSelectClause> arr = new ArrayList<>(fields.length);
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String name = field.getName();
            arr.add(new AnnotationFieldSelectClause(i, name, field.getType(), clazz));
        }
        return arr;
    }

    private static List<StringSelectClause> getStringSelectClauses(SelectFields selectFields, Class<? extends Serializable> clazz) {
        ArrayList<StringSelectClause> arr = new ArrayList<>(selectFields.fields().length);
        for (int i = 0; i < selectFields.fields().length; i++) {
            arr.add(new StringSelectClause(i, selectFields.fields()[i], clazz));
        }
        return arr;
    }

    @Override
    public List<Clause> parseWhere() {
        return List.of();
    }

    @Override
    public List<Clause> parseOrderBy() {
        return List.of();
    }

    @Override
    public List<Clause> parseGroupBy() {
        return List.of();
    }

    @Override
    public List<Clause> parseFrom() {
        return List.of();
    }


    private static class AnnotationFieldSelectClause extends StringSelectClause {

        private final Class<?> paramType;

        public AnnotationFieldSelectClause(int index, String name, @NonNull Class<?> paramType, @NonNull Class<?> clazz) {
            super(index, name, clazz);
            this.paramType = paramType;
        }

        @Override
        AnnotationFieldSelectClause convertToAnnotationClause() {
            return this;
        }

        protected void check() {

        }

        @Override
        public String toClauseString() {
            return "";
        }

    }

    @AllArgsConstructor
    @RequiredArgsConstructor
    private static class StringSelectClause implements SelectClause {

        private final int index;
        private final String name;
        private final Class<?> clazz;

        @Override
        public String toClauseString() {
            throw new UnsupportedOperationException("不支持的生成表达式");
        }

        AnnotationFieldSelectClause convertToAnnotationClause() {

            ArrayList<Class<?>> types = new ArrayList<>();
            Field[] fields = clazz.getFields();
            for (Field field : fields) {
                if (name.equals(field.getName())) {
                    types.add(field.getType());
                }
            }

            if (types.size() == 0) {
                // 查找Get方法
            }

            return new AnnotationFieldSelectClause(index, name, paramType, clazz);
        }

    }
}
