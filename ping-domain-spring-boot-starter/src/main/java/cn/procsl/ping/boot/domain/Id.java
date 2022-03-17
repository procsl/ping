package cn.procsl.ping.boot.domain;

import java.io.Serializable;

public interface Id<ID extends Serializable> extends Serializable {

    ID getId();

}
