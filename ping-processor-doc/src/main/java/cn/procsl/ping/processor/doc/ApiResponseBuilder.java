package cn.procsl.ping.processor.doc;

import cn.procsl.ping.processor.api.AbstractAnnotationSpecBuilder;
import cn.procsl.ping.processor.api.AnnotationSpecBuilder;
import cn.procsl.ping.processor.api.ProcessorContext;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * 这里需要读取配置, 需要更改
 */
@AutoService(AnnotationSpecBuilder.class)
public class ApiResponseBuilder extends AbstractAnnotationSpecBuilder<MethodSpec.Builder> {

    @Override
    protected boolean isType(String type) {
        return "CONTROLLER".equals(type);
    }

    @Override
    protected <E extends Element> void buildTargetAnnotation(ProcessorContext context, E source, MethodSpec.Builder target) {
        if (source == null) {
            context.getMessager().printMessage(Diagnostic.Kind.WARNING, "ExecutableElement is null," + ApiResponse.class.getName() + " 创建失败");
            return;
        }

        AnnotationSpec response200 = AnnotationSpec
            .builder(ApiResponse.class)
            .addMember("responseCode", "200")
            .build();

    }

    @Override
    protected Class<MethodSpec.Builder> target() {
        return MethodSpec.Builder.class;
    }
}
