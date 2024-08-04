package cn.procsl.ping.boot.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public final class SingletonReference<T> {

    private T instance;

    public void set(final T instance) {
        this.instance = instance;
    }

}
