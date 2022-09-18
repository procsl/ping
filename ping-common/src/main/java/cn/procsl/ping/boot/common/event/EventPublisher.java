package cn.procsl.ping.boot.common.event;

import java.io.Serializable;

public interface EventPublisher {

    void publish(String name, Serializable parameters) throws EventPublishException;

}
