package cn.procsl.ping.processor.restful.builder;

import cn.procsl.ping.processor.component.TypeComponent;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

class BuilderLoader implements Builder {

    BuilderLoader(ProcessingEnvironment processingEnv) {
    }

    @Override
    public void build(TypeComponent input, TypeElement serviceElement) {

    }

}
