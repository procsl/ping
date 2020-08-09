package cn.procsl.ping.boot.domain.business.service;

import cn.procsl.ping.business.tools.AbstractTree;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * 可持久化树
 *
 * @author procsl
 * @date 2020/07/28
 */
abstract class PersistenceTree<K extends Serializable, V> extends AbstractTree<K, V> {

    private final boolean load = false;

    private K key;

    private V value;

    private LinkedList<PersistenceTree<K, V>> children;

}
