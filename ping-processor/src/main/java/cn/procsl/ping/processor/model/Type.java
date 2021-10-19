package cn.procsl.ping.processor.model;

import cn.procsl.ping.processor.Model;
import lombok.Data;

import javax.lang.model.element.Modifier;
import java.util.Collection;

@Data
public class Type implements Model {

    // 是否生成接口
    boolean generatedInterface;

    // 访问控制
    Modifier modifier;

    // 当前的类型
    TypeName type;

    //  获取字段
    Collection<Field> fields;

    // 注解
    Collection<Annotation> annotations;

    //  方法
    Collection<Method> methods;

    @Override
    public String generatorType() {
        return "Type";
    }
}
