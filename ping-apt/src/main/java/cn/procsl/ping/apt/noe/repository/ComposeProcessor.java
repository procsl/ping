package cn.procsl.ping.apt.noe.repository;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.*;

final class ComposeProcessor implements TargetElementProcessor {

    private final HashMap<String, List<TargetElementProcessor>> builders = new HashMap<>();

    private final List<TargetElementProcessor> processors = Collections.singletonList(new DefaultProcessor());

    @Override
    public void build(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv,
                      TypeElement annotation, Element target) {

        String key = annotation.getQualifiedName().toString();
        var processor = builders.getOrDefault(key, processors);

        for (var builder : processor) {
            builder.build(processingEnv, roundEnv, annotation, target);
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> set = new HashSet<>();
        this.builders.forEach((k, v) -> {
            for (var builder : v) {
                set.addAll(builder.getSupportedAnnotationTypes());
            }
        });
        return Collections.unmodifiableSet(set);
    }

    public void addBuilder(TargetElementProcessor builder) {
        Set<String> types = builder.getSupportedAnnotationTypes();
        for (String type : types) {
            if (type == null || type.isEmpty()) {
                return;
            }
            this.builders.computeIfAbsent(type, k -> new ArrayList<>()).add(builder);
        }
    }

    static class DefaultProcessor implements TargetElementProcessor {

        @Override
        public void build(ProcessingEnvironment processingEnv, RoundEnvironment roundEnv,
                          TypeElement annotation, Element target) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "找不到处理器", annotation);
        }

    }

}
