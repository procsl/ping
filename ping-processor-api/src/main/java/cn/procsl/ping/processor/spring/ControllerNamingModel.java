package cn.procsl.ping.processor.spring;

import cn.procsl.ping.processor.model.NamingModel;
import lombok.RequiredArgsConstructor;

import javax.lang.model.element.TypeElement;

@RequiredArgsConstructor
public class ControllerNamingModel implements NamingModel {

    final TypeElement typeElement;

    @Override
    public String getPackageName() {
        return typeElement.getEnclosingElement().getSimpleName().toString();
    }

    @Override
    public String getName() {
        String simpleName = typeElement.getSimpleName().toString();
        String newName = simpleName.replaceAll("Service$", "Controller");

        if (newName.endsWith("Controller")) {
            return newName;
        }
        return newName + "Controller";
    }
}
