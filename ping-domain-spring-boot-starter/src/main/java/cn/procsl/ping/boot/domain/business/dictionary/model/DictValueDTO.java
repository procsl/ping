package cn.procsl.ping.boot.domain.business.dictionary.model;

import cn.procsl.ping.business.domain.DomainId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DictValueDTO implements DomainId {

    final Long id;

    final Value value;
}
