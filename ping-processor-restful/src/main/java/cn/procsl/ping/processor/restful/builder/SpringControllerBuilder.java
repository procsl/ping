package cn.procsl.ping.processor.restful.builder;

import cn.procsl.ping.processor.model.GeneralTypeName;
import cn.procsl.ping.processor.model.Type;
import cn.procsl.ping.processor.model.TypeName;
import cn.procsl.ping.processor.restful.builder.model.*;

import javax.lang.model.element.TypeElement;

public class SpringControllerBuilder implements Builder {

    String shortPackageName = "%s.gen.controller";

    @Override
    public void build(Type input, TypeElement serviceElement) {

        input.setType(getControllerTypeName(serviceElement));

        input.addAnnotation(new GeneratedAnnotation());
        input.addAnnotation(new RestControllerAnnotation(serviceElement));
        input.addAnnotation(new RequestMappingAnnotation());
        input.addAnnotation(new IndexedAnnotation());
        input.addAnnotation(new SpringValidatedAnnotation());

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
