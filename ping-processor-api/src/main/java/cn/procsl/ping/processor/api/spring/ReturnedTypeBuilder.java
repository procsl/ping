package cn.procsl.ping.processor.api.spring;

import cn.procsl.ping.processor.api.ProcessorContext;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import lombok.RequiredArgsConstructor;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class ReturnedTypeBuilder {

    final TypeMirror mirror;

    final ProcessorContext context;

    final String type = "RETURN-DTO";

    public TypeSpec createReturnDTO() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(this.toBuildReturnType()).addModifiers(Modifier.PUBLIC).addSuperinterface(TypeName.get(Serializable.class));

        // 解析mirror类型
        Types utils = context.getProcessingEnvironment().getTypeUtils();
        Element element = utils.asElement(mirror);

        List<VariableElement> fields = element.getEnclosedElements()
            .stream()
            .filter(item -> item instanceof VariableElement)
            .map(item -> (VariableElement) item)
            .collect(Collectors.toList());

        return builder.build();
    }


    // 创建返回值DTO
    public ClassName toBuildReturnType() {
        Element element = this.context.getProcessingEnvironment().getTypeUtils().asElement(mirror);
        String name = element.getSimpleName().toString() + "DTO";
        String packageName = element.getEnclosingElement().toString() + ".returned";
        return ClassName.get(packageName, name);
    }

    public CodeBlock getCaller() {
        return null;
    }

}
