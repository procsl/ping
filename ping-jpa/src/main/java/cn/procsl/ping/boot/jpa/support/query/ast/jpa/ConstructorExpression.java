package cn.procsl.ping.boot.jpa.support.query.ast.jpa;


import cn.procsl.ping.boot.jpa.support.query.Select;
import cn.procsl.ping.boot.jpa.support.query.ast.Expression;
import cn.procsl.ping.boot.jpa.support.query.ast.SelectExpression;
import cn.procsl.ping.boot.jpa.support.query.ast.StringExpression;
import cn.procsl.ping.boot.jpa.support.query.ast.ViewConstructor;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class ConstructorExpression implements Expression {


    @Setter
    private Class<?> targetType;
    final private ArrayList<SelectExpression> selects = new ArrayList<>();

    private static class NullSelect implements SelectExpression {

        @Nonnull
        @Override
        public String getAliasName() {
            return null;
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        public Select getSelectAnnotation() {
            return null;
        }

        @Override
        public boolean isInclude() {
            return true;
        }

        @Nonnull
        @Override
        public Expression getExpression() {
            return new StringExpression("null");
        }
    }

    public void addSelect(SelectExpression select) {
        this.selects.add(select);
    }

    @Override
    public String toExpString() {

        Constructor<?>[] cons = targetType.getConstructors();
        if (cons.length == 0) {
            throw new IllegalArgumentException("视图对象不存在构造函数: " + this.targetType);
        }
        for (Constructor<?> item : cons) {
            ViewConstructor i = AnnotationUtils.findAnnotation(item, ViewConstructor.class);
            if (i == null) {
                continue;
            }

            HashMap<String, SelectExpression> consSelect = new HashMap<>();
            for (SelectExpression select : this.selects) {
                if (select.isInclude()) {
                    consSelect.put(select.getAliasName(), select);
                }
            }

            List<String> ags = parseViewArguments(item, i);
            List<String> query = new ArrayList<>(ags.size());
            for (String ag : ags) {
                SelectExpression exp = consSelect.get(ag);
                if (exp == null) {
                    query.add("null");
                    continue;
                }
                if (!exp.isInclude()) {
                    query.add("null");
                    continue;
                }
                query.add(exp.getExpression().toExpString());
            }

            return "new " + this.targetType.getName() + "(" + String.join(",", query) + ")";
        }

        throw new IllegalArgumentException("视图对象不存在被标记的构造函数: " + this.targetType);

    }

    private List<String> parseViewArguments(Constructor<?> item, ViewConstructor view) {
        List<String> arguments = new ArrayList<>();
        List<String> a = Arrays.stream(view.names()).filter(Objects::nonNull).filter(i -> !i.isEmpty()).toList();
        if (a.isEmpty()) {
            Parameter[] params = item.getParameters();
            if (params.length == 0) {
                throw new IllegalArgumentException("被标记的视图构造方法无参数: " + this.targetType);
            }
            for (Parameter param : params) {
                arguments.add(param.getName());
            }
        } else {
            arguments.addAll(Arrays.asList(view.names()));
        }
        return arguments;
    }

}
