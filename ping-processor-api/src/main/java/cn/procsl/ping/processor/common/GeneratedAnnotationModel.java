package cn.procsl.ping.processor.common;

import cn.procsl.ping.processor.model.AnnotationModel;
import cn.procsl.ping.processor.model.CodeModel;
import cn.procsl.ping.processor.model.NamingModel;
import lombok.NonNull;

import javax.annotation.processing.Generated;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class GeneratedAnnotationModel implements AnnotationModel {

    final Map<String, CodeModel> codeModelHashMap = new LinkedHashMap<>();

    public GeneratedAnnotationModel(String clazz) {
        codeModelHashMap.put("value", new StringCodeModel(clazz, "$S"));
        codeModelHashMap.put("date", new StringCodeModel("", "$S"));
        codeModelHashMap.put("comments", new StringCodeModel("", "$S"));
    }

    @Override
    public NamingModel getName() {
        return new ClassNamingModel(Generated.class);
    }

    @Override
    public CodeModel getCode(@NonNull String fieldName) {
        return codeModelHashMap.get(fieldName);
    }

    @Override
    public Collection<String> getFieldNames() {
        return codeModelHashMap.keySet();
    }


}
