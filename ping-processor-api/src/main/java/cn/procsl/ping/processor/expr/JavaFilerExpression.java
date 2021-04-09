package cn.procsl.ping.processor.expr;

import cn.procsl.ping.processor.Expression;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import lombok.NonNull;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class JavaFilerExpression implements Expression<JavaFile> {

    @NonNull
    final TypeElement typeElement;
    @NonNull
    final Expression<TypeSpec> typeSpecExpression;

    JavaFilerExpression(@NonNull TypeElement typeElement, @NonNull Expression<TypeSpec> typeSpecExpression) {
        this.typeElement = typeElement;
        this.typeSpecExpression = typeSpecExpression;
    }

    public static JavaFilerExpression createJavaFiler(@NonNull TypeElement typeElement,
                                                      @NonNull Expression<TypeSpec> typeSpecExpression) {
        return new JavaFilerExpression(typeElement, typeSpecExpression);
    }

    @Override
    public JavaFile interpret(@NonNull ProcessingEnvironment environment, @NonNull RoundEnvironment roundEnvironment, @NonNull Element root) {
        String packageName = typeElement.getEnclosingElement().getSimpleName().toString();
        return JavaFile.builder(packageName, typeSpecExpression.interpret(environment, roundEnvironment, root))
            .build();
    }

}
