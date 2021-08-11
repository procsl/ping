package cn.procsl.ping.processor.api;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import lombok.RequiredArgsConstructor;

import javax.lang.model.element.ExecutableElement;

@RequiredArgsConstructor
class ReturnedBuilder {

    final GeneratorProcessor generatorProcessor;
    final String methodName;
    final String fieldName;
    final ExecutableElement item;
    final CodeBlock buildCalle;

    public TypeName buildReturnedType() {
        return null;
    }

    public String buildCodeBlack() {
        return null;
    }
}
