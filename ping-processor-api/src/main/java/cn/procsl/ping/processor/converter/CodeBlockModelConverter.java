package cn.procsl.ping.processor.converter;

import com.squareup.javapoet.CodeBlock;
import lombok.NonNull;

public enum CodeBlockModelConverter implements ModelConverter<String, CodeBlock> {

    INSTANCE;

    @Override
    public CodeBlock convertTo(@NonNull String source) {
        return CodeBlock.of(source);
    }
}
