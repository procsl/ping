package cn.procsl.ping.processor.restful.builder;

import cn.procsl.ping.processor.AbstractConfigurableProcessor;
import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Set;

@AutoService(Processor.class)
public class SpringRestfulProcessor extends AbstractConfigurableProcessor {

    Builder builder;

    @Override
    protected void init() {
        this.builder = new BuilderLoader(processingEnv);
    }

    @Override
    protected void processor(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {


    }


}
