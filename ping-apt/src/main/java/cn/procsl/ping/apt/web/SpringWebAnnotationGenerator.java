package cn.procsl.ping.apt.web;

import lombok.RequiredArgsConstructor;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@RequiredArgsConstructor
public class SpringWebAnnotationGenerator {

    protected final ProcessingEnvironment processingEnv;
    protected final RoundEnvironment roundEnvironment;
    protected final Messager messager;
    protected final Set<? extends TypeElement> annotations;
    protected final Element element;

    public void generated() {

    }

    public void write() {

    }

}
