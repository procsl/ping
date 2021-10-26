package cn.procsl.ping.processor.restful.builder;

import cn.procsl.ping.processor.model.GeneralTypeName;
import cn.procsl.ping.processor.model.Type;
import cn.procsl.ping.processor.model.TypeName;
import cn.procsl.ping.processor.restful.builder.model.*;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class SpringControllerBuilder implements Builder {

    String shortPackageName = "%s.gen.controller";

    @Override
    public void build(Type input, TypeElement serviceElement) {

        input.setModifier(Modifier.PUBLIC);

        // 添加方法类型
        input.setType(getControllerTypeName(serviceElement));

        // 添加方法注解
        input.addAnnotation(new GeneratedAnnotation());
        input.addAnnotation(new RestControllerAnnotation(serviceElement));
        input.addAnnotation(new RequestMappingAnnotation());
        input.addAnnotation(new IndexedAnnotation());
        input.addAnnotation(new SpringValidatedAnnotation());

        input.setGeneratedInterface(true);

        input.addField(new ServiceTypeField(serviceElement));
    }

    private TypeName getControllerTypeName(TypeElement serviceElement) {
        String packageName = serviceElement.getEnclosingElement().toString();
        return new GeneralTypeName(String.format(shortPackageName, packageName), createClassName(serviceElement));
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
