package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.TypeModel;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class SourceConverter implements ModelConverter<TypeModel, JavaFile> {

    ModelConverter<TypeModel, TypeSpec> typeModelTypeSpecModelConverter;

    @Override
    public JavaFile to(@NonNull TypeModel source) {
        return JavaFile.builder(source.getName().getPackageName(),
            typeModelTypeSpecModelConverter.to(source))
            .build();
    }
}
