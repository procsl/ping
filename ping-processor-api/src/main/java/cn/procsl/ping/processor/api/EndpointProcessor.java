package cn.procsl.ping.processor.api;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EndpointProcessor extends AbstractConfigurableProcessor {

    final Map<String, TypeSpec> typeSpecs = new HashMap<>();

    final Map<String, FieldSpec> fieldSpecs = new HashMap<>();

    @Override
    protected void processor(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {

    }


}
