package cn.procsl.ping.processor.common;

import cn.procsl.ping.processor.model.NamingModel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleNamingModel implements NamingModel {

    @Getter
    final String packageName;

    @Getter
    final String name;

    @SuppressWarnings("unused")
    public SimpleNamingModel(String fullName) {
        // TODO
        this.packageName = "";
        this.name = "";
    }
}
