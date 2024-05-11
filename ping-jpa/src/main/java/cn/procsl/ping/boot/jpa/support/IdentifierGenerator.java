package cn.procsl.ping.boot.jpa.support;

import java.io.Serializable;

public interface IdentifierGenerator<ID extends Serializable> {

    ID nextId(String name, ID initId);

}
