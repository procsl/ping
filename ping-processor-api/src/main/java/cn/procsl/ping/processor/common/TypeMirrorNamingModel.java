package cn.procsl.ping.processor.common;

import cn.procsl.ping.processor.model.NamingModel;
import lombok.RequiredArgsConstructor;

import javax.lang.model.type.TypeMirror;

@RequiredArgsConstructor
public class TypeMirrorNamingModel implements NamingModel {

    final TypeMirror typeMirror;

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
