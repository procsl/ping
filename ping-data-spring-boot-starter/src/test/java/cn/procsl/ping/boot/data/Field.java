package cn.procsl.ping.boot.data;

import java.io.Serializable;

/**
 * @author procsl
 * @date 2020/04/12
 */
@FunctionalInterface
public interface Field<T> {

    Serializable field();

}
