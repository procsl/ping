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
        List<? extends SelectClause> fields = this.getSelectFields();

        return getStringSelectClauses(injectable, clazz);
    }


    protected List<? extends SelectClause> getSelectFields() {

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
            return getStringSelectClauses(selectFields);
        }

        // 获取类上的 selectFields
        SelectFields clazzFields = AnnotationUtils.findAnnotation(this.clazz, SelectFields.class);
        if (clazzFields != null && clazzFields.fields().length != 0) {
            return getStringSelectClauses(clazzFields);
        }

        // 3. 通过构造函数参数匹配类字段
        // 判断是否标注了 SelectFields, 如果被标注了, 就使用被标注的构造方法对应的字段名称
        if (selectFields != null) {
            TypeVariable<? extends Constructor<?>>[] params = markConstructor.getTypeParameters();
            ArrayList<SelectClause> arr = new ArrayList<>(params.length);
            for (int i = 0; i < params.length; i++) {
                var param = params[i];
                // TODO 优先通过注解获取名字?
                name = param.getName();
                arr.add(new AnnotationFieldSelectClause(i, param.getName(), ));
            }
        }

        return null;
    }

    private static List<SelectClause> getStringSelectClauses(SelectFields selectFields) {
        ArrayList<SelectClause> arr = new ArrayList<>(selectFields.fields().length);
        for (int i = 0; i < selectFields.fields().length; i++) {
            arr.add(new StringSelectClause(i, selectFields.fields()[i]));
        }
        return arr;
    }

    private List<SelectClause> getStringSelectClauses(Constructor<?> constructor, Class<? extends Serializable> clazz) {
        // 匹配的字段名称和类型需要完全相同
        // 匹配完成后
        // 1. 如果存在构造函数, 需要和构造函数参数完全一致的顺序
        // 否则使用字段/get方法的顺序
        String[] fields = null;
        if (constructor != null) {
            // 如果构造函数
            SelectFields selectFields = AnnotationUtils.findAnnotation(constructor, SelectFields.class);
            if (selectFields != null) {
                fields = selectFields.fields();
            }
        }

        TypeVariable<? extends Constructor<?>>[] params = constructor.getTypeParameters();
        if (params.length == 0) {
            throw new IllegalArgumentException("被@InjectConstructor注解标注的构造函数无参数: " + this.clazz);
        }

        List<SelectClause> list = new ArrayList<>();
        for (int i = 0; i < params.length; i++) {
            TypeVariable<? extends Constructor<?>> param = params[i];
            AnnotationFieldSelectClause tem = new AnnotationFieldSelectClause(i, param, this.clazz);
            list.add(tem);
        }
        return list;
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

    protected List<Field> getFields() {
        Field[] fields = this.clazz.getDeclaredFields();
        return List.of(fields);
    }

    static class AnnotationFieldSelectClause extends StringSelectClause {

        final Class<? extends Serializable> clazz;

        AnnotationFieldSelectClause(int index, String name,
                                    @NonNull Class<? extends Serializable> clazz) {
            super(index, name);
            this.clazz = clazz;
        }

        @Override
        AnnotationFieldSelectClause convertToAnnotationClause(Class<? extends Serializable> clazz) {
            return this;
        }

        @Override
        public String toClauseString() {
            return "";
        }

    }

    @AllArgsConstructor
    @RequiredArgsConstructor
    static class StringSelectClause implements SelectClause {

        final int index;
        final String name;

        @Override
        public String toClauseString() {
            throw new UnsupportedOperationException("不支持的生成表达式");
        }

        AnnotationFieldSelectClause convertToAnnotationClause(Class<? extends Serializable> clazz) {
            return new AnnotationFieldSelectClause(index, name, clazz);
        }

    }
}
