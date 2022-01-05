package cn.procsl.ping.processor.v2.web;

import cn.procsl.ping.processor.ProcessorEnvironment;
import cn.procsl.ping.processor.TypeSpecBuilder;
import cn.procsl.ping.processor.v2.TypeSpecHandler;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import lombok.NonNull;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

class SpringTypeBuilder implements TypeSpecBuilder {

    List<TypeSpecHandler> handlers;

    public SpringTypeBuilder(ProcessingEnvironment processingEnv) {
        this.handlers = Arrays.asList();
    }

    @Override
    public void create(@NonNull TypeElement element, @NonNull ProcessorEnvironment env) throws IOException {
        String name = createClassName(element);
        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(name).addModifiers(Modifier.PUBLIC);
        for (TypeSpecHandler handler : this.getSortedHandlers()) {
            handler.handle(typeSpec, env);
        }

        TypeSpec result = typeSpec.build();
        // TODO to create PackageName
        String packageName = null;
        JavaFile.Builder javaFile = JavaFile.builder(packageName, result);
        javaFile.skipJavaLangImports(false);
        javaFile.build().writeTo(env.getFiler());
    }

    public Collection<TypeSpecHandler> getSortedHandlers() {
        return handlers;
    }

    String createClassName(TypeElement element) {
        String name = element.getSimpleName().toString();
        if (name.endsWith("Service")) {
            name = name.replaceAll("Application", "").replaceAll("Service" + "$", "Controller");
        } else {
            name = name + "Controller";
        }
        return name;
    }

}
