package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.GeneratorContext;
import cn.procsl.ping.processor.generator.SpecBuilder;
import com.squareup.javapoet.TypeSpec;
import lombok.RequiredArgsConstructor;

import javax.lang.model.element.TypeElement;

@RequiredArgsConstructor
public class SpringTypeSpecBuilder implements SpecBuilder<TypeElement, TypeSpec> {


    @Override
    public void build(GeneratorContext context, TypeElement source, TypeSpec type) {

    }
}
