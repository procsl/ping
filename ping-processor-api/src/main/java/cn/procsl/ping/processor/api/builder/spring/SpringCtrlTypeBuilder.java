package cn.procsl.ping.processor.api.builder.spring;

import cn.procsl.ping.processor.api.AnnotationVisitor;
import cn.procsl.ping.processor.api.Generator;
import com.squareup.javapoet.TypeSpec;
import lombok.NonNull;
import lombok.Setter;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class SpringCtrlTypeBuilder implements Generator<TypeSpec> {

    // 目标方法
    final TypeElement targetElement;

    final TypeSpec.Builder builder;

    // 是否创建接口
    @Setter
    private boolean needCreateInterface = false;

    @Setter
    private AnnotationVisitor visitor;

    public SpringCtrlTypeBuilder(@NonNull TypeElement targetElement) {
        this.targetElement = targetElement;
        String name = this.getBaseTargetElementCtrlName();
        this.builder = TypeSpec.classBuilder(name);
    }

    public SpringCtrlTypeBuilder(@NonNull TypeElement targetElement, AnnotationVisitor visitor) {
        this(targetElement);
        this.visitor = visitor;
    }

    @Override
    public TypeSpec getSpec() {

        this.builder.addModifiers(Modifier.PUBLIC);
        if (visitor != null) {
            visitor.typeVisitor(targetElement, this.builder);
        }

//        String fieldName = NamingUtils.lowerCamelCase(targetElement.getSimpleName().toString());
//        FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(TypeName.get(targetElement.asType()), fieldName, Modifier.PROTECTED);
//        visitor.fieldVisitor(targetElement, fieldSpecBuilder);
//
//        builder.addField(fieldSpecBuilder.build());
//
//        FieldSpec.Builder request = FieldSpec.builder(ClassName.get("javax.servlet.http", "HttpServletRequest"), "request", Modifier.PROTECTED);
//        visitor.fieldVisitor(targetElement, request);
//
//        builder.addField(request.build());

//        List<ExecutableElement> list = targetElement.getEnclosedElements().stream()
//            .filter(item -> item instanceof ExecutableElement)
//            .filter(this::methodSelector)
//            .map(item -> (ExecutableElement) item)
//            .collect(Collectors.toList());
//        for (ExecutableElement item : list) {
//            this.buildMethod(builder, item, fieldName);
//        }


//        String packageName = targetElement.getEnclosingElement().toString() + ".api";
//        JavaFile java = JavaFile
//            .builder(packageName, builder.build())
//            .addFileComment("这是自动生成的代码，请勿修改").build();
//
        return builder.build();
    }

    public String getBaseTargetElementCtrlName() {
        String name = targetElement.getSimpleName().toString();
        if (name.endsWith("Service")) {
            name = name.replaceAll("Application", "").replaceAll("Service" + "$", "Controller");
        } else {
            name = name + "Controller";
        }
        return name;
    }

}
