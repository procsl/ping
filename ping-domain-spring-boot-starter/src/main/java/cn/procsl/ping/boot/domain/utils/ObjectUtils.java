package cn.procsl.ping.boot.domain.utils;


import java.util.Arrays;

/**
 * @author procsl
 * @date 2020/07/08
 */
public final class ObjectUtils {

    private ObjectUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean nullSafeEquals(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1.equals(o2)) {
            return true;
        }
        if (o1.getClass().isArray() && o2.getClass().isArray()) {
            return arrayEquals(o1, o2);
        }
        return false;
    }


    private static boolean arrayEquals(Object pre, Object next) {
        if (pre instanceof Object[] && next instanceof Object[]) {
            return Arrays.equals((Object[]) pre, (Object[]) next);
        }
        if (pre instanceof boolean[] && next instanceof boolean[]) {
            return Arrays.equals((boolean[]) pre, (boolean[]) next);
        }
        if (pre instanceof byte[] && next instanceof byte[]) {
            return Arrays.equals((byte[]) pre, (byte[]) next);
        }
        if (pre instanceof char[] && next instanceof char[]) {
            return Arrays.equals((char[]) pre, (char[]) next);
        }
        if (pre instanceof double[] && next instanceof double[]) {
            return Arrays.equals((double[]) pre, (double[]) next);
        }
        if (pre instanceof float[] && next instanceof float[]) {
            return Arrays.equals((float[]) pre, (float[]) next);
        }
        if (pre instanceof int[] && next instanceof int[]) {
            return Arrays.equals((int[]) pre, (int[]) next);
        }
        if (pre instanceof long[] && next instanceof long[]) {
            return Arrays.equals((long[]) pre, (long[]) next);
        }
        if (pre instanceof short[] && next instanceof short[]) {
            return Arrays.equals((short[]) pre, (short[]) next);
        }
        return false;
    }

}
