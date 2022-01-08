package cn.procsl.ping.processor.v2.web;

import cn.procsl.ping.processor.ProcessorEnvironment;
import cn.procsl.ping.processor.utils.NamingUtils;
import cn.procsl.ping.processor.v2.SpecHandler;
import cn.procsl.ping.processor.v2.web.request.MethodHandler;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

class SpringControllerHandler implements SpecHandler<TypeSpec.Builder> {

    final String httpServletRequest = "javax.servlet.http.HttpServletRequest";

    final SpecHandler<TypeSpec.Builder> methodHandler;

    public SpringControllerHandler() {
        methodHandler = new MethodHandler();
    }

    @Override
    public void handle(Element source, TypeSpec.Builder builder, ProcessorEnvironment environment) {
        if (!(source instanceof TypeElement)) {
            return;
        }

        TypeElement fieldRef = environment.getTypeElementByName(httpServletRequest);
        if (fieldRef != null) {
            TypeName type = TypeName.get(fieldRef.asType());
            String fieldName = NamingUtils.lowerCamelCase(fieldRef.getSimpleName().toString());
            FieldSpec.Builder request = FieldSpec.builder(type, fieldName, Modifier.PROTECTED);
            builder.addField(request.build());
        }

        for (Element item : source.getEnclosedElements()) {
            this.methodHandler.handle(item, builder, environment);
        }
    }

    @Override
    public int order() {
        return Integer.MAX_VALUE - 1;
    }
}
