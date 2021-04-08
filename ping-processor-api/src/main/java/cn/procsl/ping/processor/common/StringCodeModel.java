package cn.procsl.ping.processor.common;

import cn.procsl.ping.processor.model.CodeModel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StringCodeModel implements CodeModel {

    @NonNull
    @Getter
    final String source;

    @NonNull
    @Getter
    final String format;

    public StringCodeModel(@NonNull String[] source) {
        this.source = "{" + String.join(",", source) + "}";
        this.format = "$N";
    }
}
