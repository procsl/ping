package cn.procsl.ping.boot.common;

import java.io.Serializable;

public interface ID<T extends Serializable> extends Serializable {

    T getId();

}
