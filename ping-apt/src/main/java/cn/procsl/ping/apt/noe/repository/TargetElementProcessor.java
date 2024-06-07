package cn.procsl.ping.apt.noe.repository;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import java.util.Collections;
import java.util.Set;

public interface TargetElementProcessor {

    void build(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv, TypeElement annotation, Element target);


    default Set<String> getSupportedAnnotationTypes() {
        return Collections.emptySet();
    }


}
