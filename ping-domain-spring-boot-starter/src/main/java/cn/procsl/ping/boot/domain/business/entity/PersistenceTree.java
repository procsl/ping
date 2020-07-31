package cn.procsl.ping.boot.domain.business.entity;

import cn.procsl.ping.business.domain.DomainEntity;
import cn.procsl.ping.business.tools.AbstractTree;

import java.io.Serializable;

/**
 * 可持久化树
 *
 * @author procsl
 * @date 2020/07/28
 */
public abstract class PersistenceTree<K extends Serializable, V> extends AbstractTree<K, V> implements DomainEntity {

}
