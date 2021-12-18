package cn.procsl.ping.processor.web.spring;

import cn.procsl.ping.processor.utils.CodeUtils;
import cn.procsl.ping.processor.web.AnnotationVisitor;
import cn.procsl.ping.processor.web.AnnotationVisitorLoader;
import cn.procsl.ping.processor.web.ProcessorContext;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static cn.procsl.ping.processor.utils.ClassUtils.isPersistenceEntity;
import static cn.procsl.ping.processor.utils.ClassUtils.toBuildReturnType;

class ReturnedCreator {

    private final static ClassName simpleTypeWrapper = ClassName.get("cn.procsl.ping.web", "SimpleTypeWrapper");
    private final ProcessorContext context;
    private final String methodName;
    private final String fieldName;
    private final ExecutableElement methodElement;
    private final CodeBlock buildCaller;
    private final Types utils;
    private final Elements elements;
    private final TypeName returnType;
    private final TypeMirror returned;
    private final AnnotationVisitor returnAnnotationVisitor;

    public ReturnedCreator(ProcessorContext context, String methodName, String fieldName, ExecutableElement element, CodeBlock buildCaller) {
        this.context = context;
        this.methodName = methodName;
        this.fieldName = fieldName;
        this.methodElement = element;
        this.buildCaller = buildCaller;
        this.returned = element.getReturnType();
        this.utils = this.context.getProcessingEnvironment().getTypeUtils();
        this.elements = this.context.getProcessingEnvironment().getElementUtils();
        this.returnAnnotationVisitor = new AnnotationVisitorLoader(context, "CONTROLLER_RETURNED");
        this.returnType = init();
    }

    public TypeName createReturnedType() {
        return this.returnType;
    }

    private TypeName init() {

        if (returned.getKind().toString().equals("VOID")) {
            return TypeName.get(returned);
        }

        if (CodeUtils.hasNeedWrapper(returned)) {
            TypeName returnType = TypeName.get(returned);
            return ParameterizedTypeName.get(simpleTypeWrapper, returnType);
        }

        boolean bool = returned.toString().startsWith(Optional.class.getName()) && returned instanceof DeclaredType;
        if (!bool) {
            return TypeName.get(returned);
        }

        List<? extends TypeMirror> args = ((DeclaredType) returned).getTypeArguments();
        if (args.isEmpty()) {
            return TypeName.get(returned);
        }

        TypeMirror argType = args.get(0);


        TypeName typeName = isPersistenceEntity(utils.asElement(argType)) ? toBuildReturnType(argType, utils) : ClassName.get(argType);

        if (CodeUtils.hasNeedWrapper(argType)) {
            return ParameterizedTypeName.get(simpleTypeWrapper, typeName);
        }

        // 其他
        return typeName;
    }

    public CodeBlock createCodeBlack() throws IOException {

        if (returned.getKind().toString().equals("VOID")) {
            return CodeBlock.builder().add(this.buildCaller).build();
        }

        CodeBlock.Builder start = CodeBlock.builder().add("\n").add(this.buildCaller);
        if (CodeUtils.hasNeedWrapper(returned)) {
//            TypeName returnType = TypeName.get(returned);
//            ParameterizedTypeName parameter = ParameterizedTypeName.get(simpleTypeWrapper, returnType);
            start.add("\nreturn new $T(returnObject);", this.returnType).add("\n");
            return start.build();
        }

        boolean bool = returned.toString().startsWith(Optional.class.getName()) && returned instanceof DeclaredType;
        if (!bool) {
            return start.add("\nreturn returnObject;").add("\n").build();
        }

        List<? extends TypeMirror> args = ((DeclaredType) returned).getTypeArguments();
        if (args.isEmpty()) {
            return start.add("\nreturn returnObject;").add("\n").build();
        }

        TypeMirror argType = args.get(0);
        String tmp = " if (returnObject.isEmpty()) {\n\t  throw new $T($N, $S, $S); \n\t} \n $T option = returnObject.get(); \n";

        ClassName exp = ClassName.get("cn.procsl.ping.business.exception", "BusinessException");
        CodeBlock notFound = CodeBlock.builder().add(tmp, exp, "404", "H001", "Not Found", TypeName.get(argType)).build();

        start.add(notFound);

        // 是否需要包装
        if (CodeUtils.hasNeedWrapper(argType)) {
            return start.add("\nreturn new $T(option);", simpleTypeWrapper).add("\n").build();
        }

        // 是否需要将entity转换成dto
        if (!isPersistenceEntity(utils.asElement(argType))) {
            return start.add("\nreturn option;").add("\n").build();
        }

        String argName = "option";

        DataTransferObjectBuilder builder = new DataTransferObjectBuilder(elements, utils, this.returnAnnotationVisitor, argType, argName);

        builder.toWrite(this.context.getFiler());

        return start.add(builder.getCaller()).add("\nreturn dto;\n").build();
    }


}
