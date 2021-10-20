package cn.procsl.ping.processor.model;

import cn.procsl.ping.processor.Model;
import lombok.Getter;
import lombok.Setter;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Collection;

public class Type implements Model {

    // 是否生成接口
    @Setter
    @Getter
    boolean generatedInterface;

    // 访问控制
    @Setter
    @Getter
    Modifier modifier;

    // 当前的类型
    @Setter
    @Getter
    TypeName type;

    //  获取字段
    Collection<Field> fields = new ArrayList<>();

    // 注解
    Collection<Annotation> annotations = new ArrayList<>();

    //  方法
    Collection<Method> methods = new ArrayList<>();

    @Override
    public String generatorType() {
        return "Type";
    }

    public void addField(Field fields) {
        this.fields.add(fields);
    }

    public void addAnnotation(Annotation annotations) {
        this.annotations.add(annotations);
    }

    public void addMethod(Method methods) {
        this.methods.add(methods);
    }
}
