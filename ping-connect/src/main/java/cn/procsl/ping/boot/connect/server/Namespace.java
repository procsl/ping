package cn.procsl.ping.boot.connect.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@SuppressWarnings("unused")
public @interface Namespace {
    String name() default "/";

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @SuppressWarnings("unused")
    @interface OnceEvent {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @SuppressWarnings("unused")
    @interface Event {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @SuppressWarnings("unused")
    @interface Disconnect {
        String disconnect = "disconnect";
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @SuppressWarnings("unused")
    @interface Disconnecting {
        String disconnecting = "disconnecting";
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @SuppressWarnings("unused")
    @interface Connect {
        String connect = "connect";
    }

}
