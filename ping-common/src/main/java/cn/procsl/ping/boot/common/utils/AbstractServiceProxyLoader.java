package cn.procsl.ping.boot.common.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AbstractServiceProxyLoader<T> {

    final Class<T> target;

    @Getter
    private List<T> service;

    public final void load() {
        if (service != null) {
            return;
        }
        synchronized (target) {
            if (service != null) {
                return;
            }
            ServiceLoader<T> serviceLoader = ServiceLoader.load(target);
            List<T> tmp = serviceLoader.stream().map(ServiceLoader.Provider::get).collect(Collectors.toList());
            this.service = Collections.unmodifiableList(this.onLoad(tmp));
        }
    }

    protected List<T> onLoad(List<T> tmp) {
        return tmp;
    }

}
