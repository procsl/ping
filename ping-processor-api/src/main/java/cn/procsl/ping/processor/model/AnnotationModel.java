package cn.procsl.ping.processor.model;

import lombok.Data;

import java.util.Map;

@Data
public class AnnotationModel {

    NamingModel type;

    String name;

    Map<String, Value> valueMap;

    static class Value {
        Type type;

        String value;
    }

    enum Type {
        STRING, ENUM, TYPE
    }
}
