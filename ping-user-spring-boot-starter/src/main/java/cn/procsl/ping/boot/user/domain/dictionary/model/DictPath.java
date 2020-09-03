package cn.procsl.ping.boot.user.domain.dictionary.model;

import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * 少量依赖了 spring jpa
 * 数据字典节点
 */
@Setter
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)// for jpa
@Embeddable
public class DictPath implements AdjacencyPathNode<Long> {

    @Column(nullable = false, updatable = false)
    protected Long pathId;

    @Column(nullable = false, updatable = false)
    protected Integer seq;

    public DictPath(@NonNull Long pathId, @NonNull Integer seq) {
        this.pathId = pathId;
        this.seq = seq;
    }
}
