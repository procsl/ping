package cn.procsl.ping.processor.common;

import cn.procsl.ping.processor.model.CodeModel;

public enum EmptyCodeModel implements CodeModel {
    INSTANCE;


    @Override
    public String getSource() {
        return "";
    }

    @Override
    public String getFormat() {
        return "$N";
    }
}
