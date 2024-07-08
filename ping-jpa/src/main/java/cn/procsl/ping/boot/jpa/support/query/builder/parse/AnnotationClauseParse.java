package cn.procsl.ping.boot.jpa.support.query.builder.parse;

import cn.procsl.ping.boot.jpa.support.query.SelectFields;
import cn.procsl.ping.boot.jpa.support.query.builder.Clause;
import cn.procsl.ping.boot.jpa.support.query.builder.QueryClauseParse;
import cn.procsl.ping.boot.jpa.support.query.builder.SelectClause;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.List;

@RequiredArgsConstructor
public final class AnnotationClauseParse implements QueryClauseParse {

    final Class<? extends Serializable> clazz;

    final SelectParser selectParser = new SelectParser();

    @Override
    public List<SelectClause> parseSelects() {

        // 解析被标记的构造函数
        Constructor<?> constructor = selectParser.parseMarkedConstructors(this.clazz);
        // 先解析构造函数上的
        SelectFields selectFields;
        if (constructor != null) {
            selectFields = this.selectParser.parseSelectFields(constructor);
            if (selectFields != null) {
                return this.selectParser.parseClauses(constructor, selectFields);
            }
        }

        // 再解析pojo上的
        selectFields = this.selectParser.parseSelectFields(this.clazz);

        // 使用默认的
        if (selectFields == null) {
            selectFields = this.selectParser.createDefaultSelectFields(this.clazz);
        }

        return this.selectParser.parseClauses(this.clazz, selectFields);
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

}
