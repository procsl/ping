package cn.procsl.ping.boot.domain.business.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PathUtils {

    /**
     * 判断是否未子串
     *
     * @param dec    分隔符
     * @param path   路径
     * @param prefix 字串
     * @return 如果为子串, 则返回1, 如果相等则返回0, 不为子串则返回-1
     */
    public static int isSubPath(@NonNull String path, @NonNull String prefix, @NonNull String dec) {

        if (path.equals(prefix)) {
            return 0;
        }

        path = standardize(path, dec);
        prefix = standardize(prefix, dec);

        if (prefix.equals(path)) {
            return 0;
        }

        if (prefix.length() >= path.length()) {
            return -1;
        }

        boolean tmp = path.startsWith(prefix);
        if (tmp) {
            return path.charAt(prefix.length() - 1) == dec.charAt(0) ? 1 : -1;
        }
        return -1;
    }

    /**
     * 分割路径
     *
     * @param path 指定的路径
     * @param dec  分割符
     * @return 返回分割后的path段
     */
    public static List<String> split(String path, @NonNull String dec) {
        path = standardize(path, dec);
        return Arrays.
            stream(path.split(dec + ""))
            .filter(item -> !StringUtils.isEmpty(item))
            .collect(Collectors.toUnmodifiableList());
    }

    /**
     * path去重
     *
     * @param paths 路径列表
     * @param dec   分隔符
     * @return 返回去重后的path
     */
    public static List<String> distinct(@NonNull Collection<String> paths, @NonNull String dec) {
        if (paths.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> list = paths
            .stream()
            .distinct()
            .filter(item -> !PathUtils.isEmpty(item, dec))
            .sorted(Comparator.comparingInt(String::length))
            .collect(Collectors.toList());

        for (int i = 0; i < list.size(); i++) {
            String prefix = list.get(i);
            list.removeIf(path -> isSubPath(path, prefix, dec) == 1);
        }
        return list;
    }

    /**
     * 标准化路径
     *
     * @param path 指定的路径
     * @param dec  分隔符
     * @return 返回标准化的路径
     */
    public static String standardize(String path, @Nonnull String dec) {
        if (isEmpty(path, dec)) {
            return dec;
        }

        boolean start = path.startsWith(dec);
        if (start) {
            path = dec.concat(path);
        }

        boolean end = path.endsWith(dec);
        if (end) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * 判断是否为根节点
     *
     * @param path 路径
     * @param des  分隔符
     * @return 如果为根节点, 则返回true
     */
    public static boolean isRoot(String path, @Nonnull String des) {
        if (isEmpty(path, des)) {
            return true;
        }

        String stand = standardize(path, des);
        for (int i = 1; i < stand.length(); i++) {
            if (stand.charAt(i) == des.charAt(0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否为空节点
     *
     * @param path 指定的path
     * @param dec  分隔符
     * @return 如果为空节点返回true
     */
    public static boolean isEmpty(String path, @Nonnull String dec) {
        if (StringUtils.isEmpty(path)) {
            return true;
        }

        path = path.trim();
        if (StringUtils.isEmpty(path)) {
            return true;
        }

        return dec.equals(path);
    }

}
