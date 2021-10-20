package cn.procsl.ping.processor.model;

import cn.procsl.ping.processor.Model;

import java.util.List;

public interface Code extends Model {

    Code EMPTY_CODE = new EmptyCode();

    String getTemplate();

    List<String> getParameters();

    @Override
    default String generatorType() {
        return "Code";
    }


}
