package cn.procsl.ping.processor.restful;

import cn.procsl.ping.processor.AnnotationVisitor;
import cn.procsl.ping.processor.ProcessorContext;
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
    public void typeVisitor(Element element, TypeSpec.Builder spec) {
        for (AnnotationVisitor visitor : current) {
            visitor.typeVisitor(element, spec);
        }
    }

    @Override
    public void fieldVisitor(Element element, FieldSpec.Builder spec) {
        for (AnnotationVisitor visitor : current) {
            visitor.fieldVisitor(element, spec);
        }
    }

    @Override
    public void methodVisitor(Element element, MethodSpec.Builder spec) {
        for (AnnotationVisitor visitor : current) {
            visitor.methodVisitor(element, spec);
        }
    }

    @Override
    public void parameterVisitor(Element element, ParameterSpec.Builder spec) {
        for (AnnotationVisitor visitor : current) {
            visitor.parameterVisitor(element, spec);
        }
    }

    @Override
    public void variableVisitor(Element element, ParameterSpec.Builder spec) {

        for (AnnotationVisitor visitor : current) {
            visitor.variableVisitor(element, spec);
        }
    }
}
