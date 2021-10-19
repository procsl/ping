package cn.procsl.ping.processor.model;

import lombok.Data;

@Data
public class GeneralTypeName implements TypeName {

    String packageName;
    String className;

    @Override
    public String generatorType() {
        return "generalTypeName";
    }

}
