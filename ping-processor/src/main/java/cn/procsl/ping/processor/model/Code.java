package cn.procsl.ping.processor.model;

import cn.procsl.ping.processor.Model;
import lombok.Data;

import java.util.List;

@Data
public class Code implements Model {

    // 代码模板
    String codeTemplate;

    // 代码占位符参数
    List<String> codeParameters;


    @Override
    public String generatorType() {
        return "Code";
    }
}
