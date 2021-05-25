package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.model.AnnotationModel;
import cn.procsl.ping.processor.model.NamingModel;

import java.util.Date;
import java.util.Map;

public class GeneratedAnnotationBuilder extends AnnotationModel {

    public GeneratedAnnotationBuilder() {
        super(new NamingModel("javax.annotation.processing", "Generated"));
        Map<String, String> now = Map.of("value", String.format("{%s}", new Date()));
        this.setValueMap(now);
    }

}
