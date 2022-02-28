package cn.procsl.ping.web;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
public class SimpleTypeWrapper<T> implements Serializable {

    final T result;

}
