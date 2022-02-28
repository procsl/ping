package cn.procsl.ping.business.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DomainIdEntity<ID> implements DomainId<ID> {
    final ID id;
}
