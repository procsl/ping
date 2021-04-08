package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.AnnotationModel;
import cn.procsl.ping.processor.model.CodeModel;
import cn.procsl.ping.processor.model.NamingModel;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@SuperBuilder
public class AnnotationConverter extends AbstractAwareConvertor<AnnotationModel, AnnotationSpec> {

    @NonNull
    private final ModelConverter<NamingModel, ClassName> namingModelClassNameConverter;

    @NonNull
    private final ModelConverter<CodeModel, CodeBlock> codeBlockModelConverter;

    @Override
    protected AnnotationSpec convertTo(AnnotationModel source) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(this.namingModelClassNameConverter.to(source.getName()));

        Collection<String> fields = source.getFieldNames();

        for (String field : fields) {
            CodeModel code = source.getCode(field);
            builder.addMember(field, code.getFormat(), code.getSource());
        }
        return builder.build();
    }

}
