package cn.procsl.ping.boot.domain.business;

import java.io.Serializable;

public interface SystemConfiguration<ID extends Serializable> extends Serializable {

    String getName();

    String getConfigText();

    String getDescription();

}
