package cn.procsl.ping.processor.restful.builder;

import cn.procsl.ping.processor.AbstractConfigurableProcessor;
import cn.procsl.ping.processor.component.TypeComponent;
import cn.procsl.ping.processor.restful.builder.component.*;
import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Set;

@AutoService(Processor.class)
public class SpringRestfulProcessor extends AbstractConfigurableProcessor {


    @Override
    protected void processor(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {

        GeneratedAnnotationComponent generatorAnnotation = new GeneratedAnnotationComponent(this.getClass().getName());

        ServiceTypeFieldComponent serviceField = new ServiceTypeFieldComponent();

        TypeComponent<TypeElement> component = new SpringControllerComposite();
        component.addChild(generatorAnnotation);
        component.addChild(serviceField);
        component.addChild(new IndexedAnnotationComponent());
        component.addChild(new ControllerRequestMappingAnnotationComponent());
        component.addChild(new RestControllerAnnotationComponent());

    }


}
