package cn.procsl.ping.boot.domain.business;

import java.io.Serializable;

public interface Configuration extends Serializable {

    String getKey();

    String getConfigText();

    ConfigurationType getType();

    String getDescription();

    enum ConfigurationType {
        String, Number, Null, Tree, Object, Json
    }

}
