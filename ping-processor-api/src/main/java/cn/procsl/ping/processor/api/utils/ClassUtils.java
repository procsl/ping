package cn.procsl.ping.processor.api.utils;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClassUtils {


    public static boolean exists(@NonNull String name) {

        try {
            Class.forName(name);
        } catch (Exception e) {
            return false;
        }
        return true;
    }


}
