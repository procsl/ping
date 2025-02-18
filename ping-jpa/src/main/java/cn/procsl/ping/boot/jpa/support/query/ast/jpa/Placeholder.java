package cn.procsl.ping.boot.jpa.support.query.ast.jpa;

import cn.procsl.ping.boot.jpa.support.query.ast.Variable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 变量表达式
 */
@Getter
@RequiredArgsConstructor
public final class Placeholder implements Variable {

    final private String name;
    final private Field field;
    final private Object target;

    @Override
    public String toExpString() {
        return name;
    }


    @Override
    @SneakyThrows
    public Object getValue() {
        List<Method> methods = ClassUtils.extractGetAndIsMethods(target.getClass());

        String fieldName = this.field.getName();
        String t = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        String getter = "get" + t;
        String is = "is" + t;
        for (Method method : methods) {
            if (method.getName().equals(getter)) {
                return method.invoke(this.target);
            }
            if (method.getName().equals(is)) {
                return method.invoke(this.target);
            }
        }

        return this.field.get(this.target);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Placeholder that = (Placeholder) o;

        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }


    @Override
    public String toString() {
        return this.toExpString();
    }
}
