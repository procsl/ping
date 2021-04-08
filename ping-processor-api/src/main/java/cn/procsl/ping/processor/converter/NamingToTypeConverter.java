package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.NamingModel;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import lombok.NonNull;

public enum NamingToTypeConverter implements ModelConverter<NamingModel, TypeName> {

    INSTANCE;

    @Override
    public TypeName to(@NonNull NamingModel source) {
        return ClassName.get(source.getPackageName(), source.getName());
    }
}
