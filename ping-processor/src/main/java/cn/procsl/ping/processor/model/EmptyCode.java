package cn.procsl.ping.processor.model;

import java.util.Collections;
import java.util.List;

final class EmptyCode implements Code {

    @Override
    public String generatorType() {
        return "empty-code";
    }

    public String getTemplate() {
        return "";
    }

    public List<String> getParameters() {
        return Collections.emptyList();
    }
}
