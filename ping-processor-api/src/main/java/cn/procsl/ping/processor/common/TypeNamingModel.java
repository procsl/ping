package cn.procsl.ping.processor.common;

import cn.procsl.ping.processor.model.NamingModel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.lang.model.element.TypeElement;

@RequiredArgsConstructor
public class TypeNamingModel implements NamingModel {

    @NonNull
    final TypeElement typeElement;

    @Override
    public String getPackageName() {
        return typeElement.getEnclosingElement().toString();
    }

    @Override
    public String getName() {
        return typeElement.getSimpleName().toString();
    }
}
