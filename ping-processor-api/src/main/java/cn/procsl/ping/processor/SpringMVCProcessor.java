package cn.procsl.ping.processor;

import cn.procsl.ping.processor.builder.ControllerTypeBuilder;
import cn.procsl.ping.processor.model.TypeModel;
import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.Set;

@SupportedAnnotationTypes({"javax.ws.rs.Path"})
@Slf4j
//@AutoService(Processor.class)
public class SpringMVCProcessor extends AbstractConfigurableProcessor {

    @Override
    protected void processor(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Path.class);
        elements.stream().filter(item -> item instanceof ExecutableElement)
            .map(item -> (ExecutableElement) item)
            .map(Element::getEnclosingElement)
            .filter(item -> item instanceof TypeElement)
            .map(item -> (TypeElement) item)
            .distinct()
            .forEach(item -> {
                TypeModel type = new ControllerTypeBuilder(item, "api", this.processingEnv);
            });
    }
}
