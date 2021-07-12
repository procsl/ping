package cn.procsl.ping.processor.doc;

import cn.procsl.ping.processor.api.AbstractGeneratorBuilder;
import cn.procsl.ping.processor.api.GeneratorBuilder;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.lang.model.element.Element;

/**
 * 这里需要读取配置, 需要更改
 */
@AutoService(GeneratorBuilder.class)
public class SpringDocBuilder extends AbstractGeneratorBuilder {

    @Override
    public void methodAnnotation(String type, Element element, MethodSpec.Builder spec) {
        super.methodAnnotation(type, element, spec);

        AnnotationSpec response200 = AnnotationSpec
            .builder(ApiResponse.class)
            .addMember("responseCode", "200")
            .build();
    }

    @Override
    public void typeAnnotation(String type, Element source, TypeSpec.Builder spec) {

        String tmp = context.getProcessingEnvironment().getElementUtils().getDocComment(source);
        JavaCommentResolver comment = new JavaCommentResolver(tmp);
        String name = comment.findOndTag("@name");
        String description = comment.findOndTag("@description");

        if (name == null) {
            name = source.getSimpleName().toString().replaceAll("Service$", "接口");
        }

        if (description == null) {
            description = tmp;
        }

        AnnotationSpec tagAnnotation = AnnotationSpec
            .builder(ClassName.get(Tag.class))
            .addMember("name", "$S", name)
            .addMember("description", "$S", description)
            .build();
        spec.addAnnotation(tagAnnotation);
    }

    @Override
    public boolean support(String type) {
        return "CONTROLLER".equals(type);
    }
}
