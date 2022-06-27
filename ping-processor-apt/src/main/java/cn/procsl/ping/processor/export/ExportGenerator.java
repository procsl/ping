package cn.procsl.ping.processor.export;

import cn.procsl.ping.processor.Generator;
import cn.procsl.ping.processor.annotation.ExportMethod;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.stream.Collectors;

class ExportGenerator implements Generator {

    ProcessingEnvironment processingEnv;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    @Override
    public boolean process(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(ExportMethod.class);
        Set<TypeElement> types = elements.stream().map(Element::getEnclosingElement).filter(item -> item instanceof TypeElement).map(item -> (TypeElement) item).collect(Collectors.toSet());
        for (TypeElement type : types) {

            String name = type.getSimpleName().toString();
            name = name.replace("Service", "");
            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(String.format("%sAdapter", name));
            typeBuilder.addModifiers(Modifier.PUBLIC);

            for (Element enclosedElement : type.getEnclosedElements()) {
                if (!(enclosedElement instanceof ExecutableElement)) {
                    continue;
                }

                ExportMethod export = enclosedElement.getAnnotation(ExportMethod.class);
                if (export == null) {
                    continue;
                }

                ExecutableElement item = (ExecutableElement) enclosedElement;
                MethodSpec.Builder builder = MethodSpec.methodBuilder(item.getSimpleName().toString());
                item.getParameters().forEach(p -> builder.addParameter(ParameterSpec.get(p)).build());

            }
        }


        return true;
    }

}
