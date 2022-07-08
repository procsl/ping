package cn.procsl.ping.boot.common.advice;

public @interface OverrideTarget {

    Strategy strategy() default Strategy.primary;

    enum Strategy {
        primary, advice
    }

}
