package cn.procsl.ping.processor.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GeneralTypeName implements TypeName {

    final String packageName;

    final String className;


    @Override
    public String generatorType() {
        return "generalTypeName";
    }

}
