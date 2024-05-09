package cn.procsl.ping.boot.common.event;

import java.io.Serializable;
import java.util.EventObject;
import java.util.function.Consumer;

public interface EventBusBridge {

    String publisher(String name, Serializable parameters) throws EventPublishException;

    void subscriber(String name, Consumer<EventObject> consumer);

}
