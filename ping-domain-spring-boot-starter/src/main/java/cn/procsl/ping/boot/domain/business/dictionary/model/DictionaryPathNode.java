package cn.procsl.ping.boot.domain.business.dictionary.model;

import cn.procsl.ping.boot.domain.business.tree.model.AdjacencyPathNode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;

/**
 * 数据字典节点
 */
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)// for jpa
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class DictionaryPathNode implements AdjacencyPathNode<Long> {

    @Column(nullable = false, updatable = false)
    protected Long pathId;

    @Column(nullable = false, updatable = false)
    protected String space;

    @Column(nullable = false, updatable = false)
    protected Integer seq;

}
