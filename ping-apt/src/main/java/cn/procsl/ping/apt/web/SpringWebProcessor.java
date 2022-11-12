package cn.procsl.ping.apt.web;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

//@AutoService(Processor.class)
@SupportedAnnotationTypes("cn.procsl.ping.boot.common.web.*")
public class SpringWebProcessor extends AbstractProcessor {


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            return true;
        }

        Set<? extends Element> rootElements = roundEnv.getElementsAnnotatedWithAny(
                annotations.toArray(new TypeElement[0]));
        if (rootElements.isEmpty()) {
            return true;
        }
        Messager messager = this.processingEnv.getMessager();
        for (Element element : rootElements) {
            messager.printMessage(Diagnostic.Kind.NOTE, "开始处理: " + element.toString());
            try {
                SpringWebAnnotationGenerator generator = new SpringWebAnnotationGenerator(processingEnv
                        , roundEnv, messager, annotations, element);
                generator.generated();
            } catch (Exception e) {
                messager.printMessage(Diagnostic.Kind.WARNING, "生成[" + element + "]错误: " + e.getMessage());
            }
        }
        return true;
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
