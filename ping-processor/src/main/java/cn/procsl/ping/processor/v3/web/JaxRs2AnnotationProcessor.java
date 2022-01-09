package cn.procsl.ping.processor.v3.web;

import cn.procsl.ping.processor.ProcessorEnvironment;
import cn.procsl.ping.processor.SimpleEnvironment;
import cn.procsl.ping.processor.v3.web.descriptor.RequestMethodDescriptor;
import cn.procsl.ping.processor.v3.web.generator.ParameterGenerator;
import cn.procsl.ping.processor.v3.web.generator.ReturnGenerator;
import cn.procsl.ping.processor.v3.web.generator.SpringControllerGenerator;
import cn.procsl.ping.processor.v3.web.parser.RequestMethodParser;
import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.ws.rs.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 生成 SpringController 类
 */
@AutoService(Processor.class)
@SuppressWarnings("unused")
public class JaxRs2AnnotationProcessor extends AbstractProcessor {

    final ServiceElementVisitor visitor = new ServiceElementVisitor();
    final Collection<JavaSourceGenerator> generators;

    public JaxRs2AnnotationProcessor() {
        generators = Arrays.asList(new SpringControllerGenerator(), new ParameterGenerator(), new ReturnGenerator());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        SimpleEnvironment env = new SimpleEnvironment(this.processingEnv, roundEnv);

        List<RequestMethodDescriptor> list = annotations
            .stream()
            .map(item -> item.accept(visitor, env))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        for (JavaSourceGenerator generator : generators) {
            try {
                generator.generated(list, env);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Path.class.getName());
    }

    private static class ServiceElementVisitor implements ElementVisitor<RequestMethodDescriptor, ProcessorEnvironment> {

        final RequestMethodParser parser = new RequestMethodParser();

        @Override
        public RequestMethodDescriptor visit(Element e, ProcessorEnvironment environment) {
            return null;
        }

        @Override
        public RequestMethodDescriptor visitPackage(PackageElement e, ProcessorEnvironment environment) {
            return null;
        }

        @Override
        public RequestMethodDescriptor visitType(TypeElement e, ProcessorEnvironment environment) {
            return null;
        }

        @Override
        public RequestMethodDescriptor visitVariable(VariableElement e, ProcessorEnvironment environment) {
            return null;
        }

        @Override
        public RequestMethodDescriptor visitExecutable(ExecutableElement e, ProcessorEnvironment environment) {
            return parser.parser(e, environment);
        }

        @Override
        public RequestMethodDescriptor visitTypeParameter(TypeParameterElement e, ProcessorEnvironment environment) {
            return null;
        }

        @Override
        public RequestMethodDescriptor visitUnknown(Element e, ProcessorEnvironment environment) {
            return null;
        }
    }
}
