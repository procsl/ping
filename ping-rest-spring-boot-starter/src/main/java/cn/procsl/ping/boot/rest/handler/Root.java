package cn.procsl.ping.boot.rest.handler;

import lombok.Value;

import java.io.Serializable;

@Value
public class Root<T> implements Serializable {
    T value;
}
