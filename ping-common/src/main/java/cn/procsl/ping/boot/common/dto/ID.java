package cn.procsl.ping.boot.common.dto;

import java.io.Serializable;

public interface ID<T extends Serializable> {

    T getId();

}
