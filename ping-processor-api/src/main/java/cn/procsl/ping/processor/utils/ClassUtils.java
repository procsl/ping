package cn.procsl.ping.processor.utils;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassUtils {


    public static boolean exists(@NonNull String name) {

        try {
            Class.forName(name);
        } catch (Exception e) {
            return false;
        }
        return true;
    }


}
