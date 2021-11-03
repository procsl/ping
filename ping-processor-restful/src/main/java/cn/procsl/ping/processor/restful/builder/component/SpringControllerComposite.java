package cn.procsl.ping.processor.restful.builder.component;

import cn.procsl.ping.processor.ProcessorContext;
import cn.procsl.ping.processor.component.AbstractComponent;
import cn.procsl.ping.processor.component.TypeComponent;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;

public class SpringControllerComposite extends AbstractComponent<TypeSpec, TypeElement> implements TypeComponent<TypeElement> {


    String createClassName(TypeElement e) {
        String name = e.getSimpleName().toString();
        if (name.endsWith("Service")) {
            name = name.replaceAll("Application", "").replaceAll("Service" + "$", "Controller");
        } else {
            name = name + "Controller";
        }
        return name;
    }


    @Override
    public TypeSpec.Builder builder(ProcessorContext context, TypeElement element) {
        return TypeSpec.classBuilder(this.createClassName(element));
    }
}
