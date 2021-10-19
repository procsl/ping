package cn.procsl.ping.processor.model;

import cn.procsl.ping.processor.Model;
import lombok.Data;

import javax.lang.model.element.Modifier;
import java.util.Collection;

@Data
public class Field implements Model {


    // 访问控制
    Modifier modifier;
    // 字段名称
    String name;
    // 字段类型
    TypeName type;
    // 字段注解
    Collection<Annotation> fieldAnnotations;

    @Override
    public String generatorType() {
        return "Field";
    }
}
