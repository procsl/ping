package cn.procsl.ping.processor.api.spring;

import cn.procsl.ping.processor.api.AbstractGeneratorProcessor;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
public class ReturnedGenerator extends AbstractGeneratorProcessor {

    @Override
    protected TypeSpec generated(TypeElement annotation, RoundEnvironment roundEnv) {
        return null;
    }

    @Override
    protected String getPackage(TypeElement annotation) {
        return null;
    }

    @Override
    protected String generatedType() {
        return null;
    }
}
