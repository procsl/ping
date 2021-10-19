package cn.procsl.ping.processor.restful.builder;

import cn.procsl.ping.processor.model.Annotation;
import cn.procsl.ping.processor.model.ClassTypeName;
import cn.procsl.ping.processor.model.TypeName;
import cn.procsl.ping.processor.restful.aware.ProcessorAware;

import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

public class GeneratedAnnotationBuilder implements Builder<Annotation, TypeElement>, ProcessorAware {


    ProcessingEnvironment processingEnv;

    @Override
    public Annotation build(Annotation input, TypeElement element, RoundEnvironment roundEnv) {
        return new Annotation() {
            @Override
            public TypeName getType() {
                return new ClassTypeName(Generated.class);
            }

            @Override
            public String generatorType() {
                return "annotation";
            }
        };
    }


    @Override
    public void setProcessingEnv(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }
}
