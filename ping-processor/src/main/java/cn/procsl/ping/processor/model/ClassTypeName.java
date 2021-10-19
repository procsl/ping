package cn.procsl.ping.processor.model;

import lombok.Data;

@Data
public class ClassTypeName implements TypeName {

    final Class<?> clazz;

    @Override
    public String generatorType() {
        return "type-name";
    }

    @Override
    public String getPackageName() {
        return clazz.getPackageName();
    }

    @Override
    public String getClassName() {
        return clazz.getName();
    }
}
