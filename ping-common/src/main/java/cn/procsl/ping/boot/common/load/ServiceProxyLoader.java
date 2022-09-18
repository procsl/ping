package cn.procsl.ping.boot.common.load;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class ServiceProxyLoader<T> {

    final Class<T> target;

    @Getter
    protected List<T> service;

    public final void load() {
        if (service != null) {
            return;
        }
        synchronized (target) {
            if (service != null) {
                return;
            }
            ServiceLoader<T> serviceLoader = ServiceLoader.load(target);
            this.service = serviceLoader.stream().map(ServiceLoader.Provider::get).collect(Collectors.toList());
        }
    }

}
