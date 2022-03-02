package cn.procsl.ping.boot.domain.business;

import java.io.Serializable;

public interface SystemConfigurationRepository<T extends SystemConfiguration<ID>, ID extends Serializable> {

    T findOne(String name);

}
