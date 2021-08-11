package cn.procsl.ping.processor.api.spring;

import cn.procsl.ping.processor.api.AbstractGeneratedProcessor;
import cn.procsl.ping.processor.api.utils.NamingUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
public class ControllerGenerated extends AbstractGeneratedProcessor {

    final static String CONTROLLER = "Controller";

    final static String SERVICE = "Service";

    @Override
    protected TypeSpec generated(TypeElement typeElement, RoundEnvironment roundEnv) {
        String name = this.createClassName(typeElement);

        TypeSpec.Builder builder = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);

        String fieldName = NamingUtils.lowerCamelCase(typeElement.getSimpleName().toString());
        FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(TypeName.get(typeElement.asType()), fieldName, Modifier.PROTECTED);

        builder.addField(fieldSpecBuilder.build());

        FieldSpec.Builder request = FieldSpec.builder(ClassName.get("javax.servlet.http", "HttpServletRequest"), "request", Modifier.PROTECTED);
        builder.addField(request.build());

//        typeElement.getEnclosedElements().fo
//        MethodSpec.methodBuilder();

        return builder.build();
    }

    @Override
    protected String getPackage(TypeElement annotation) {
        return null;
    }

    @Override
    protected String generatedType() {
        return CONTROLLER;
    }

    String createClassName(TypeElement element) {
        String name = element.getSimpleName().toString();
        if (name.endsWith(SERVICE)) {
            name = name.replaceAll("Application", "").replaceAll(SERVICE + "$", CONTROLLER);
        } else {
            name = name + CONTROLLER;
        }
        return name;
    }


}
