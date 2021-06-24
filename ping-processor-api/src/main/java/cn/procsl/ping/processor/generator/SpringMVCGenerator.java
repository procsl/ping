package cn.procsl.ping.processor.generator;

import cn.procsl.ping.processor.CodeGenerator;
import cn.procsl.ping.processor.GeneratorContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.ServiceLoader;

@AutoService(CodeGenerator.class)
public class SpringMVCGenerator implements CodeGenerator {


    final ServiceLoader<TypeAnnotationSpecBuilder> typeAnnotationSpecBuilders = ServiceLoader.load(TypeAnnotationSpecBuilder.class);


    @Override
    public void generate(GeneratorContext generatorContext) throws IOException {

        TypeElement element = this.selector(generatorContext);

        String name = this.createClassName(element);

        TypeSpec.Builder builder = TypeSpec.classBuilder(name);

        for (TypeAnnotationSpecBuilder specBuilder : typeAnnotationSpecBuilders) {
            AnnotationSpec spec = specBuilder.build(generatorContext, element);
            if (spec != null) {
                builder.addAnnotation(spec);
            }
        }

        JavaFile java = JavaFile
            .builder("cn.procsl.ping.gen.web.rest", builder.build())
            .addFileComment("这是自动生成的代码， 请勿修改").build();
        java.writeTo(generatorContext.getFiler());
    }

    @Override
    public String getSupportAnnotation() {
        return Path.class.toString();
    }

    String createClassName(TypeElement element) {
        String name = element.getSimpleName().toString();
        if (name.endsWith("Service")) {
            name = name.replaceAll("Service$", "Controller");
        } else {
            name = name + "Controller";
        }
        return name;
    }


    TypeElement selector(GeneratorContext generatorContext) {
        for (TypeElement annotation : generatorContext.getAnnotations()) {
            return annotation;
        }
        return null;
    }

}
