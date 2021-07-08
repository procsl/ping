package cn.procsl.ping.processor.doc;

import cn.procsl.ping.processor.api.AbstractAnnotationSpecBuilder;
import cn.procsl.ping.processor.api.AnnotationSpecBuilder;
import cn.procsl.ping.processor.api.ProcessorContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * 注释规范, 如果存在 @name 标记的文本, 则会被转化为 name 属性, 如果不存在, 则使用类名,并除去 "Service", "Controller"等
 * 如果存在 @description 标记的文本, 会被转化为 description 属性, 如果不存在, 则将所有的文本转换为  description
 */
@AutoService(AnnotationSpecBuilder.class)
public class TagBuilder extends AbstractAnnotationSpecBuilder<TypeSpec.Builder> {


    @Override
    protected boolean isType(String type) {
        return "CONTROLLER".equals(type);
    }

    @Override
    protected <E extends Element> void buildTargetAnnotation(ProcessorContext context, E source, TypeSpec.Builder target) {

        if (source == null) {
            context.getMessager().printMessage(Diagnostic.Kind.WARNING, "TypeElement is null," + Tag.class.getName() + " 创建失败");
            return;
        }

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
        target.addAnnotation(tagAnnotation);
    }

    @Override
    protected Class<TypeSpec.Builder> target() {
        return TypeSpec.Builder.class;
    }
}
