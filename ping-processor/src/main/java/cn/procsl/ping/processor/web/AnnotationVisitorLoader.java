package cn.procsl.ping.processor.web;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public final class AnnotationVisitorLoader implements AnnotationVisitor {

    private static List<AnnotationVisitor> allVisitor;
    private final Object lock = new Object();
    private final String type;
    private List<AnnotationVisitor> current;

    public AnnotationVisitorLoader(ProcessorContext context, String type) {
        this.type = type;
        if (allVisitor == null) {
            synchronized (lock) {
                ServiceLoader<AnnotationVisitor> generator = ServiceLoader.load(AnnotationVisitor.class, this.getClass().getClassLoader());
                allVisitor = generator.stream().map(ServiceLoader.Provider::get).collect(Collectors.toList());
                for (AnnotationVisitor visitor : allVisitor) {
                    visitor.init(context);
                }
            }
        }

        current = new ArrayList<>(allVisitor.size());
        for (AnnotationVisitor visitor : allVisitor) {
            if (visitor.support().equals(type)) {
                current.add(visitor);
            }
        }

    }

    @Override
    public void init(ProcessorContext context) {
    }

    @Override
    public String support() {
        return this.type;
    }

    @Override
    public void visitor(Element element, TypeSpec.Builder spec) {
        for (AnnotationVisitor visitor : current) {
            visitor.visitor(element, spec);
        }
    }

    @Override
    public void visitor(Element element, FieldSpec.Builder spec) {
        for (AnnotationVisitor visitor : current) {
            visitor.visitor(element, spec);
        }
    }

    @Override
    public void visitor(Element element, MethodSpec.Builder spec) {
        for (AnnotationVisitor visitor : current) {
            visitor.visitor(element, spec);
        }
    }

    @Override
    public void visitor(Element element, ParameterSpec.Builder spec) {
        for (AnnotationVisitor visitor : current) {
            visitor.visitor(element, spec);
        }
    }

    @Override
    public void variableVisitor(Element element, ParameterSpec.Builder spec) {

        for (AnnotationVisitor visitor : current) {
            visitor.variableVisitor(element, spec);
        }
    }
}
