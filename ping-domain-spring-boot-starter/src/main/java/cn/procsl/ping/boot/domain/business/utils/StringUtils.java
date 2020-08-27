package cn.procsl.ping.boot.domain.business.utils;

import org.springframework.lang.Nullable;

/**
 * String 工具
 *
 * @author procsl
 * @date 2020/07/08
 */
public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean isEmpty(@Nullable String str) {
        return (str == null || str.isEmpty());
    }

}
