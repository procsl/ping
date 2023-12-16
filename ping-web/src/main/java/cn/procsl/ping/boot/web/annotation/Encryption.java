package cn.procsl.ping.boot.web.annotation;

/**
 * 请求解密/响应加密
 */
public @interface Encryption {

    /**
     * 解密请求
     */
    boolean request() default true;

    /**
     * 加密响应
     */
    boolean response() default true;

}
