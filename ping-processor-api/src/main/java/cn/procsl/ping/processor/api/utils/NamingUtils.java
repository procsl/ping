package cn.procsl.ping.processor.api.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NamingUtils {

    /**
     * 小驼峰命名法
     *
     * @param name 名称
     * @return 返回小驼峰命名法明明后的字符串
     */
    public static String lowerCamelCase(@NonNull String name) {
        if (name.isEmpty()) {
            return name;
        }
        String first = String.valueOf(name.charAt(0)).toLowerCase();
        return first + name.substring(1);
    }

    /**
     * 返回大驼峰命名法的字符串
     *
     * @param name 名称
     * @return 大驼峰字符串
     */
    public static String upperCamelCase(@NonNull String name) {
        if (name.isEmpty()) {
            return name;
        }
        String first = String.valueOf(name.charAt(0)).toUpperCase();
        return first + name.substring(1);
    }
}
