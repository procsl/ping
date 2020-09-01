package cn.procsl.ping.boot.user.domain.dictionary.model;

import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import static cn.procsl.ping.boot.user.domain.dictionary.model.Dictionary.SPACE_NAME_LEN;

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

    @Column(length = SPACE_NAME_LEN, nullable = false)
    protected String space;

    public DictPath(@NonNull Long pathId, @NonNull Integer seq, @NonNull String space) {
        this.pathId = pathId;
        this.seq = seq;
        this.space = space;
    }
}
