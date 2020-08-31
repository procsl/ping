package cn.procsl.ping.boot.domain.business.dictionary.repository;

import cn.procsl.ping.boot.domain.business.dictionary.model.QDictionary;
import cn.procsl.ping.business.domain.DomainId;
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
    <T extends DomainId<Long>> T search(@NotNull String path,
                                        @NotNull boolean ignoreActive, @NotNull Expression<T> select)
        throws IllegalArgumentException;

}
