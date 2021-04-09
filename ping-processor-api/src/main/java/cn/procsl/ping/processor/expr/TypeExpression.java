package cn.procsl.ping.processor.expr;

import cn.procsl.ping.processor.Expression;
import cn.procsl.ping.processor.utils.CodeUtils;
import com.squareup.javapoet.*;
import lombok.Builder;
import lombok.NonNull;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Collection;

@Builder
public class TypeExpression implements Expression<TypeSpec> {

    @NonNull
    final TypeElement typeElement;

    Collection<Expression<AnnotationSpec>> annotationExpressions;

    Collection<Expression<FieldSpec>> fieldExpressions;

    Collection<Expression<MethodSpec>> methodExpressions;

    Collection<Expression<TypeName>> interfaceTypes;

    Collection<Expression<TypeVariableName>> variableExpressions;

    Expression<TypeName> superClassType;

    Expression<CodeBlock> initCodeBlock;

    Expression<CodeBlock> staticCodeBlock;

    String createName() {
        String simpleName = typeElement.getSimpleName().toString();
        String newName = simpleName.replaceAll("Service$", "Controller");

        if (newName.endsWith("Controller")) {
            return newName;
        }
        return newName + "Controller";
    }

    @Override
    public TypeSpec interpret(@NonNull ProcessingEnvironment environment,
                              @NonNull RoundEnvironment roundEnvironment,
                              @NonNull Element root) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(createName());
        builder.addModifiers(Modifier.PUBLIC);
        builder.addAnnotations(CodeUtils.foreach(annotationExpressions, environment, roundEnvironment, root));
        builder.addFields(CodeUtils.foreach(fieldExpressions, environment, roundEnvironment, root));
        builder.addSuperinterfaces(CodeUtils.foreach(interfaceTypes, environment, roundEnvironment, root));
        builder.addMethods(CodeUtils.foreach(methodExpressions, environment, roundEnvironment, root));
        builder.addTypeVariables(CodeUtils.foreach(variableExpressions, environment, roundEnvironment, root));
        if (initCodeBlock != null) {
            builder.addInitializerBlock(initCodeBlock.interpret(environment, roundEnvironment, root));
        }
        if (superClassType != null) {
            builder.superclass(superClassType.interpret(environment, roundEnvironment, root));
        }
        if (staticCodeBlock != null) {
            builder.addStaticBlock(staticCodeBlock.interpret(environment, roundEnvironment, root));
        }
        return builder.build();
    }
}
