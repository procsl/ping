package cn.procsl.ping.processor.converter;

import cn.procsl.ping.processor.model.VariableNamingModel;
import com.squareup.javapoet.TypeVariableName;
import lombok.NonNull;

public enum VariableTypeConverter implements ModelConverter<VariableNamingModel, TypeVariableName> {

    INSTANCE;

    @Override
    public TypeVariableName to(@NonNull VariableNamingModel source) {
        // TODO
        return null;
    }
}
