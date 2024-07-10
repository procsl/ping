package cn.procsl.ping.boot.jpa.support.query.builder.parser;

import cn.procsl.ping.boot.jpa.support.query.*;
import cn.procsl.ping.boot.jpa.support.query.builder.Clause;
import cn.procsl.ping.boot.jpa.support.query.builder.QueryClauseParser;
import cn.procsl.ping.boot.jpa.support.query.builder.SelectClause;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@RequiredArgsConstructor
public final class AnnotationClauseParser implements QueryClauseParser {

    final Class<?> clazz;

    final static AnnotationParserHelper helper = new AnnotationParserHelper();

    @Override
    public List<SelectClause> parseSelects() {

        // 解析被标记的构造函数
        Constructor<?> constructor = helper.parseMarkedConstructor(this.clazz);
        // 先解析构造函数上的
        SelectFields selectFields;
        if (constructor != null) {
            selectFields = helper.parseSelectFields(constructor);
            if (selectFields != null) {
                return helper.parseClauses(constructor, selectFields);
            }
        }

        // 再解析pojo上的
        selectFields = helper.parseSelectFields(this.clazz);

        // 使用默认的
        if (selectFields == null) {
            selectFields = helper.createDefaultSelectFields(this.clazz);
        }

        return helper.parseClauses(this.clazz, selectFields);
    }


    @Override
    public List<Clause> parseWhere() {
        List<Clause> list = new ArrayList<>();
        Function<AnnotatedElement, WhereClause> creator = WhereClause::new;
        List<WhereClause> from = this.getClauses(ele -> helper.createByElement(ele, From.class, creator));
        List<WhereClause> where = this.getClauses(ele -> helper.createByElement(ele, Where.class, creator));
        list.addAll(from);
        list.addAll(where);
        return list;
    }

    @Override
    public List<Clause> parseOrderBy() {
        Function<AnnotatedElement, Clause> creator = (item) -> null;
        return this.getClauses(ele -> helper.createByElement(ele, OrderBy.class, creator));
    }

    @Override
    public List<Clause> parseGroupBy() {
        Function<AnnotatedElement, Clause> creator = (item) -> null;
        return this.getClauses(ele -> helper.createByElement(ele, GroupBy.class, creator));
    }

    <T> List<T> getClauses(Function<AnnotatedElement, T> func) {
        Field[] fields = this.clazz.getDeclaredFields();
        List<AnnotatedElement> list = new ArrayList<>(Arrays.asList(fields));

        Method[] methods = this.clazz.getMethods();
        for (Method method : methods) {
            boolean bool = helper.filterGetter(method);
            if (!bool) {
                continue;
            }
            list.add(method);
        }
        return list.stream().map(func).filter(Objects::nonNull).toList();
    }

    @Override
    public List<Clause> parseFrom() {
        Function<AnnotatedElement, FromClause> creator = FromClause::new;
        List<Clause> list = new ArrayList<>();
        List<FromClause> from = this.getClauses(ele -> helper.createByElement(ele, From.class, creator));
        List<FromClause> join = this.getClauses(ele -> helper.createByElement(ele, Join.class, creator));
        list.addAll(from);
        list.addAll(join);
        return list;
    }
}
