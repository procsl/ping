package cn.procsl.ping.processor.model;

import cn.procsl.ping.processor.Model;
import lombok.Data;

import javax.lang.model.element.Modifier;
import java.util.Collection;

@Data
public class Method implements Model {

    // 是否生成接口
    boolean generatedInterface;

    // 访问控制
    Modifier modifier;

    // 方法名称
    String name;

    // 方法返回值
    TypeName returnType;

    // 参数
    Collection<Parameter> parameters;

    // 方法体
    Code body;

    @Override
    public String generatorType() {
        return "Method";
    }
}
