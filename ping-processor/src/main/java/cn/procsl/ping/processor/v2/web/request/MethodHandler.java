package cn.procsl.ping.processor.v2.web.request;

import cn.procsl.ping.processor.ProcessorEnvironment;
import cn.procsl.ping.processor.v2.SpecHandler;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class MethodHandler implements SpecHandler<TypeSpec.Builder> {

    protected final List<SpecHandler<Supplier<MethodSpec.Builder>>> methodHandlers;

    public MethodHandler() {
        this.methodHandlers = Arrays.asList(new GetMethodHandler(), new PostMethodHandler(), new DeleteMethodHandler(), new PatchMethodHandler(), new PutMethodHandler());
    }

    @Override
    public void handle(Element element, TypeSpec.Builder builder, ProcessorEnvironment environment) {
        if (!(element instanceof ExecutableElement)) {
            return;
        }


        Supplier<MethodSpec.Builder> creator = () -> {
            MethodSpec.Builder temp = MethodSpec.methodBuilder(element.getSimpleName().toString()).addModifiers(Modifier.PUBLIC);

            ExecutableElement methodEle = ((ExecutableElement) element);
            // 添加返回值, 这里需要包装
            TypeMirror realTypeName = methodEle.getReturnType();
            String wrapperName = environment.getWrapperType(realTypeName);
            // TODO

            // 添加异常
            for (TypeMirror thrownType : methodEle.getThrownTypes()) {
                temp.addException(TypeName.get(thrownType));
            }

            return temp;
        };
        for (SpecHandler<Supplier<MethodSpec.Builder>> handler : this.methodHandlers) {
            handler.handle(element, creator, environment);
        }
    }
}
