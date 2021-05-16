package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.NamingModel;
import com.squareup.javapoet.TypeName;
import lombok.NonNull;

public enum NamingModeConverter implements ModelConverter<NamingModel, TypeName> {

    INSTANCE;

    @Override
    public TypeName convertTo(@NonNull NamingModel source) {
        return null;
    }
}
