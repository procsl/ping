package cn.procsl.ping.boot.user.domain.dictionary.repository;

import cn.procsl.ping.boot.user.domain.dictionary.model.QDictPath;
import cn.procsl.ping.boot.user.domain.dictionary.model.QDictionary;
import cn.procsl.ping.business.domain.DomainId;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import lombok.NonNull;

import java.util.List;
import java.util.function.Function;

public interface CustomDictionaryRepository {

    QDictionary D = QDictionary.dictionary;

    QDictPath P = QDictPath.dictPath;


    /**
     * 获取指定path的值
     *
     * @param path         以分割符分割的路径 例如: /root/profile/config
     * @param ignoreActive 是否忽略任意节点上的active
     * @return 如果找到, 则返回指定的值
     * @throws IllegalArgumentException 如果分割符不合法,则抛出此异常
     */
    List<Long> search(@NonNull String path, @NonNull boolean ignoreActive) throws IllegalArgumentException;

    /**
     * 获取指定path的值
     *
     * @param nodes        以分割符分割的路径 例如: /root/profile/config
     * @param ignoreActive 是否忽略任意节点上的active
     * @return 如果找到, 则返回指定的值的id
     * @throws IllegalArgumentException 如果分割符不合法,则抛出此异常
     */
    List<Long> search(@NonNull List<String> nodes, @NonNull boolean ignoreActive) throws IllegalArgumentException;

    /**
     * 最大化搜索路径
     *
     * @param selector 选择器
     * @param nodes    路径
     * @param fun      条件回调函数
     * @param <T>      返回值类型
     * @return 如果搜索成功, 返回与nodes下标对应的类型列表
     */
    <T extends DomainId<Long>> List<T> search(@NonNull Expression<T> selector,
                                              @NonNull List<String> nodes,
                                              Function<Integer, Predicate> fun) throws IllegalArgumentException;

    /**
     * 最大化搜索路径
     *
     * @param <T>      返回值类型
     * @param selector 选择器
     * @param path     路径
     * @param fun      条件回调函数
     * @return 如果搜索成功, 返回与nodes下标对应的类型列表
     */
    <T extends DomainId<Long>> List<T> search(@NonNull Expression<T> selector,
                                              @NonNull String path,
                                              Function<Integer, Predicate> fun) throws IllegalArgumentException;
}
