package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.CodeModel;
import com.squareup.javapoet.CodeBlock;
import lombok.NonNull;

public enum CodeModelConverter implements ModelConverter<CodeModel, CodeBlock> {

    INSTANCE;

    @Override
    public CodeBlock to(@NonNull CodeModel source) {
        return CodeBlock.of(source.getFormat(), source.getSource());
    }

}
