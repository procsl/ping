package cn.procsl.ping.processor.api;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public final class GeneratedVisitorLoader implements GeneratedVisitor {

    private static List<GeneratedVisitor> allVisitor;
    private final SupportType type;
    private final Object lock = new Object();
    private List<GeneratedVisitor> current;

    public GeneratedVisitorLoader(ProcessorContext context, SupportType type) {
        this.type = type;
        if (allVisitor == null) {
            synchronized (lock) {
                ServiceLoader<GeneratedVisitor> generator = ServiceLoader.load(GeneratedVisitor.class, this.getClass().getClassLoader());
                allVisitor = generator.stream().map(ServiceLoader.Provider::get).collect(Collectors.toList());
                for (GeneratedVisitor visitor : allVisitor) {
                    visitor.init(context);
                }
            }
        }

        current = new ArrayList<>(allVisitor.size());
        for (GeneratedVisitor visitor : allVisitor) {
            if (visitor.support().equals(type)) {
                current.add(visitor);
            }
        }

    }

    @Override
    public void init(ProcessorContext context) {
    }

    @Override
    public SupportType support() {
        return this.type;
    }

    @Override
    public void typeVisitor(Element element, TypeSpec.Builder spec) {
        for (GeneratedVisitor visitor : current) {
            visitor.typeVisitor(element, spec);
        }
    }

    @Override
    public void fieldVisitor(Element element, FieldSpec.Builder spec) {
        for (GeneratedVisitor visitor : current) {
            visitor.fieldVisitor(element, spec);
        }
    }

    @Override
    public void methodVisitor(Element element, MethodSpec.Builder spec) {
        for (GeneratedVisitor visitor : current) {
            visitor.methodVisitor(element, spec);
        }
    }

    @Override
    public void parameterVisitor(Element element, ParameterSpec.Builder spec) {
        for (GeneratedVisitor visitor : current) {
            visitor.parameterVisitor(element, spec);
        }
    }

    @Override
    public void variableVisitor(Element element, ParameterSpec.Builder spec) {

        for (GeneratedVisitor visitor : current) {
            visitor.variableVisitor(element, spec);
        }
    }
}
