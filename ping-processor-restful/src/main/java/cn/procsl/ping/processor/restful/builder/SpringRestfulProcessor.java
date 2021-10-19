package cn.procsl.ping.processor.restful.builder;

import cn.procsl.ping.processor.AbstractConfigurableProcessor;
import cn.procsl.ping.processor.model.Type;
import cn.procsl.ping.processor.restful.convert.Convertor;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Set;

@AutoService(Processor.class)
public class SpringRestfulProcessor extends AbstractConfigurableProcessor {

    Builder<Type, TypeElement> builder;

    Convertor<JavaFile, Type> convertor;

    @Override
    protected void init() {
        // TODO
    }

    @Override
    protected void processor(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {

        TypeElement target = annotations.iterator().next();

        Type type = builder.build(new Type(), target, roundEnv);

        JavaFile javaFile = convertor.convertTo(type);

        writeTo(javaFile);
    }

    void writeTo(JavaFile javaFile) throws IOException {
        javaFile.writeTo(this.filer);
    }


}
