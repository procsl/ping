package cn.procsl.ping.boot.common.id;

import java.io.Serializable;

public interface ID<T extends Serializable> extends Serializable {

    T getId();

}
