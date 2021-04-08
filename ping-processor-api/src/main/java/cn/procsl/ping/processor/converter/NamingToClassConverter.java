package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.NamingModel;
import com.squareup.javapoet.ClassName;
import lombok.NonNull;

public enum NamingToClassConverter implements ModelConverter<NamingModel, ClassName> {
    INSTANCE;

    @Override
    public ClassName to(@NonNull NamingModel source) {
        return ClassName.get(source.getPackageName(), source.getName());
    }
}
