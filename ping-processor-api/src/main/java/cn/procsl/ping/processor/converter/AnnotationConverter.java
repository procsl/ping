package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.AnnotationModel;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import lombok.experimental.SuperBuilder;

@SuperBuilder
class AnnotationConverter implements ModelConverter<AnnotationModel, AnnotationSpec> {

    @Override
    public AnnotationSpec convertTo(AnnotationModel source) {
        ClassName className = ClassName.get(source.getType().getPackageName(), source.getType().getTypeName());
        AnnotationSpec.Builder builder = AnnotationSpec.builder(className);
        source.getValueMap().forEach(builder::addMember);
        return builder.build();
    }

}
