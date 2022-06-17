package cn.procsl.ping.boot.domain.advice;

public @interface OverrideTarget {

    Strategy strategy() default Strategy.primary;

    enum Strategy {
        primary, advice
    }

}
