package cn.procsl.ping.boot.domain.business.dictionary.model;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DictDTO extends DictValueDTO {

    final String path;

    final Integer parentId;

    final Integer depth;

    final Boolean active;

    @Builder
    public DictDTO(Long id, Value value, String path,
                   Integer parentId, Integer depth,
                   Boolean active) {
        super(id, value);
        this.path = path;
        this.parentId = parentId;
        this.depth = depth;
        this.active = active;
    }
}
