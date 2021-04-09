package cn.procsl.ping.processor.expr;

import cn.procsl.ping.processor.Expression;
import cn.procsl.ping.processor.utils.CodeUtils;
import cn.procsl.ping.processor.utils.NamingUtils;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import lombok.Builder;
import lombok.NonNull;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.Collection;

@Builder
public class FieldExpression implements Expression<FieldSpec> {

    @NonNull
    final TypeElement typeElement;

    Collection<Expression<AnnotationSpec>> annotationExpressions;

    @Override
    public FieldSpec interpret(@NonNull ProcessingEnvironment environment,
                               @NonNull RoundEnvironment roundEnvironment,
                               @NonNull Element root) {
        FieldSpec.Builder builder = FieldSpec.builder(TypeName.get(typeElement.asType()), this.getFieldName(), Modifier.PROTECTED);
        builder.addAnnotations(CodeUtils.foreach(annotationExpressions, environment, roundEnvironment, root));
        return builder.build();
    }

    String getFieldName() {
        return NamingUtils.lowerCamelCase(typeElement.getSimpleName().toString());
    }
}
