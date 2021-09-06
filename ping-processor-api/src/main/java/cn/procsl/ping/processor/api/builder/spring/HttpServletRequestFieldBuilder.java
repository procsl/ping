package cn.procsl.ping.processor.api.builder.spring;

import cn.procsl.ping.processor.api.AnnotationVisitor;
import cn.procsl.ping.processor.api.AnnotationVisitorLoader;
import cn.procsl.ping.processor.api.Generator;
import cn.procsl.ping.processor.api.ProcessorContext;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;

import javax.lang.model.element.Modifier;

public class HttpServletRequestFieldBuilder implements Generator<FieldSpec> {


    private final AnnotationVisitorLoader visitor;
//    private final TypeElement element;

    public HttpServletRequestFieldBuilder(ProcessorContext context) {
        this.visitor = new AnnotationVisitorLoader(context, AnnotationVisitor.SupportType.CONTROLLER);
//        element = context.getProcessingEnvironment().getElementUtils().getTypeElement("javax.servlet.http.HttpServletRequest");
    }

    @Override
    public FieldSpec getSpec() {
        FieldSpec.Builder request = FieldSpec.builder(ClassName.get("javax.servlet.http", "HttpServletRequest"), "request", Modifier.PROTECTED);
        visitor.fieldVisitor(null, request);
        return request.build();
    }

}
