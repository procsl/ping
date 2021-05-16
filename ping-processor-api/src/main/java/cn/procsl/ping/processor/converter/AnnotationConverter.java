package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.AnnotationModel;
import cn.procsl.ping.processor.model.NamingModel;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
class AnnotationConverter implements ModelConverter<AnnotationModel, AnnotationSpec> {

    @NonNull
    private final ModelConverter<NamingModel, TypeName> namingModelClassNameConverter;

    @NonNull
    private final ModelConverter<String, CodeBlock> codeBlockModelConverter;

    @Override
    public AnnotationSpec convertTo(AnnotationModel source) {
//        AnnotationSpec.Builder builder = AnnotationSpec.builder(this.namingModelClassNameConverter.convertTo(source.getName()));

//        Collection<String> fields = source.getType();
//
//        for (String field : fields) {
//            CodeModel code = source.getCode(field);
//            builder.addMember(field, code.getFormat(), code.getSource());
//        }
//        return builder.build();
        return null;
    }

}
