package cn.procsl.ping.processor.common;

import cn.procsl.ping.processor.model.NamingModel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClassNamingModel implements NamingModel {

    @NonNull
    final Class<?> clazz;

    @Override
    public String getPackageName() {
        return clazz.getPackageName();
    }

    @Override
    public String getName() {
        return clazz.getSimpleName();
    }
}
