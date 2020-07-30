package cn.procsl.ping.boot.data.business.utils;

import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import static lombok.AccessLevel.PRIVATE;

/**
 * String 工具
 *
 * @author procsl
 * @date 2020/07/08
 */
@NoArgsConstructor(access = PRIVATE)
public class StringUtils {

    public static boolean isEmpty(@Nullable String str) {
        return (str == null || str.isEmpty());
    }

}
