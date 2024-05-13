package cn.procsl.ping.boot.common.utils;

import java.io.Serializable;

public interface IdentifierGenerator<ID extends Serializable> {

    ID nextId(String name, ID initId);

}
