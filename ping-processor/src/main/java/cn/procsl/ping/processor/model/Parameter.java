package cn.procsl.ping.processor.model;

import cn.procsl.ping.processor.Model;
import lombok.Data;

import javax.lang.model.element.Modifier;
import java.util.Collection;


@Data
public class Parameter implements Model {

    // 访问控制
    Modifier modifier;
    // 类型
    TypeName type;
    // 名称
    String name;
    // 注解
    Collection<Annotation> annotations;
    // 位置
    int index;

    @Override
    public String generatorType() {
        return "Parameter";
    }

}
