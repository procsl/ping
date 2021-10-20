package cn.procsl.ping.processor.restful.builder;

import cn.procsl.ping.processor.model.Type;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

class BuilderLoader implements Builder {

    BuilderLoader(ProcessingEnvironment processingEnv) {
    }

    @Override
    public void build(Type input, TypeElement serviceElement) {

    }

}
