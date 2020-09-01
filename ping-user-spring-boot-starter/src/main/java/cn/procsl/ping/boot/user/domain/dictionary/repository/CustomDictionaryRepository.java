package cn.procsl.ping.boot.user.domain.dictionary.repository;

import cn.procsl.ping.boot.user.domain.dictionary.model.QDictionary;
import com.querydsl.core.types.Expression;

import javax.validation.constraints.NotNull;

public interface CustomDictionaryRepository {

    QDictionary dict = QDictionary.dictionary;


    /**
     * 获取指定path的值
     *
     * @param path         以分割符分割的路径 例如: /root/profile/config
     * @param ignoreActive 是否忽略任意节点上的active
     * @return 如果找到, 则返回指定的值
     * @throws IllegalArgumentException 如果分割符不合法,则抛出此异常
     */
    <T> T search(@NotNull Expression<T> select, @NotNull String path, @NotNull boolean ignoreActive)
        throws IllegalArgumentException;

}
