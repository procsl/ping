package cn.procsl.ping.processor.generator;

import cn.procsl.ping.processor.CodeGenerator;
import cn.procsl.ping.processor.GeneratorContext;
import cn.procsl.ping.processor.builder.SpringTypeSpecBuilder;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;

@AutoService(CodeGenerator.class)
public class SpringMVCGenerator implements CodeGenerator {

    final SpringTypeSpecBuilder spec = new SpringTypeSpecBuilder();

    @Override
    public void generate(GeneratorContext generatorContext) {

        TypeElement element = this.selector(generatorContext);

        String name = this.createClassName(element);

        TypeSpec type = TypeSpec.classBuilder(name).build();

        spec.build(generatorContext, element, type);
    }

    String createClassName(TypeElement element) {
        return null;
    }


    TypeElement selector(GeneratorContext generatorContext) {
        return null;
    }

}
