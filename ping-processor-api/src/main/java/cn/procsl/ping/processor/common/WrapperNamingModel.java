package cn.procsl.ping.processor.common;

import cn.procsl.ping.processor.model.NamingModel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class WrapperNamingModel implements NamingModel {

    @NonNull
    final NamingModel namingModel;

    @Setter
    String packageName;

    @Setter
    String name;

    @Override
    public String getPackageName() {
        if (packageName == null || packageName.isEmpty()) {
            return namingModel.getPackageName();
        }
        return packageName;
    }

    @Override
    public String getName() {
        if (name == null || name.isEmpty()) {
            return namingModel.getName();
        }
        return name;
    }
}
