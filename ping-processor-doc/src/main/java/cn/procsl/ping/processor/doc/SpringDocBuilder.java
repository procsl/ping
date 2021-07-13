package cn.procsl.ping.processor.doc;

import cn.procsl.ping.processor.api.AbstractGeneratorBuilder;
import cn.procsl.ping.processor.api.GeneratorBuilder;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.lang.model.element.Element;

/**
 * 这里需要读取配置, 需要更改
 */
@AutoService(GeneratorBuilder.class)
public class SpringDocBuilder extends AbstractGeneratorBuilder {

    @Override
    public void methodAnnotation(String type, Element element, MethodSpec.Builder spec) {
        JavaCommentResolver comment = resolver(element);

        AnnotationSpec.Builder operation = AnnotationSpec.builder(Operation.class);
        String commentStr = comment.getGeneralComment();
        if (commentStr == null) {
            operation.addMember("summary", "$S", "此接口暂无描述");
        } else if (commentStr.length() < 15) {
            operation.addMember("summary", "$S", commentStr);
        } else {
            operation.addMember("summary", "$S", "详细信息见描述");
            operation.addMember("description", "$S", commentStr);
        }
        spec.addAnnotation(operation.build());
    }

    private JavaCommentResolver resolver(Element element) {
        String tmp = context.getProcessingEnvironment().getElementUtils().getDocComment(element);
        return new JavaCommentResolver(tmp);
    }

    @Override
    public void typeAnnotation(String type, Element source, TypeSpec.Builder spec) {

        JavaCommentResolver comment = resolver(source);
        String name = comment.getName();
        String description = comment.getDescription();

        if (name == null) {
            name = source.getSimpleName().toString().replaceAll("Service$", "接口");
        }

        if (description == null) {
            description = comment.getGeneralComment();
        }

        AnnotationSpec tagAnnotation = AnnotationSpec
            .builder(ClassName.get(Tag.class))
            .addMember("name", "$S", name)
            .addMember("description", "$S", description)
            .build();
        spec.addAnnotation(tagAnnotation);
    }

    @Override
    public void parameterAnnotation(String type, Element element, ParameterSpec.Builder spec) {
        JavaCommentResolver comment = this.resolver(element.getEnclosingElement());
        String description = comment.getParameterComment(element.getSimpleName().toString());
        if (description == null) {
            return;
        }
        AnnotationSpec parameter = AnnotationSpec.builder(Parameter.class)
            .addMember("description", "$S", description)
            .build();
        spec.addAnnotation(parameter);
    }

    @Override
    public boolean support(String type) {
        return "CONTROLLER".equals(type);
    }
}
