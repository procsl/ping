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
        String name = "";
        String description = "";
        if (source != null) {
            description = context.getProcessingEnvironment().getElementUtils().getDocComment(source);
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
