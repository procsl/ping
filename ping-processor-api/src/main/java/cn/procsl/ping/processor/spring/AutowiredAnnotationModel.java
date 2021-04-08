package cn.procsl.ping.processor.spring;

import cn.procsl.ping.processor.common.SimpleNamingModel;
import cn.procsl.ping.processor.common.StringCodeModel;
import cn.procsl.ping.processor.model.AnnotationModel;
import cn.procsl.ping.processor.model.CodeModel;
import cn.procsl.ping.processor.model.NamingModel;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
public class AutowiredAnnotationModel implements AnnotationModel {

    final boolean required;

    public AutowiredAnnotationModel() {
        this.required = true;
    }

    @Override
    public NamingModel getName() {
        return new SimpleNamingModel("org.springframework.beans.factory.annotation", "Autowired");
    }

    @Override
    public CodeModel getCode(String fieldName) {
        return new StringCodeModel(Boolean.toString(required), "$N");
    }

    @Override
    public Collection<String> getFieldNames() {
        return Collections.singleton("required");
    }


}
