package cn.procsl.ping.business.domain;

import javax.persistence.Id;
import java.io.Serializable;

/**
 * 实体ID标记接口
 *
 * @param <ID>
 */
public interface DomainId<ID> extends Serializable {
    @Id
    ID getId();
}
