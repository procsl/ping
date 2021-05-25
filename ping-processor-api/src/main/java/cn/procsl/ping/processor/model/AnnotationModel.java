package cn.procsl.ping.processor.model;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Data
@RequiredArgsConstructor
public class AnnotationModel {

    @NonNull NamingModel type;

    Map<String, String> valueMap;

    public AnnotationModel(@NonNull NamingModel type, Map<String, String> valueMap) {
        this.type = type;
        this.valueMap = valueMap;
    }
}
