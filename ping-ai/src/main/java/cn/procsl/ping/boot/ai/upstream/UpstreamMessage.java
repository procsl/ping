package cn.procsl.ping.boot.ai.upstream;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 客户端发往服务端的消息。
 *
 * <p>只有 {@code type} 是公共字段，其余字段按类型增减。
 * 与下游 box 的固定四字段形成有意的不对称：下游是统一分片流，上游是异构命令集。</p>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserCommand.class, name = "user"),
        @JsonSubTypes.Type(value = ConfirmCommand.class, name = "confirm"),
        @JsonSubTypes.Type(value = CancelCommand.class, name = "cancel"),
        @JsonSubTypes.Type(value = AckCommand.class, name = "ack"),
        @JsonSubTypes.Type(value = PullCommand.class, name = "pull")
})
public sealed interface UpstreamMessage
        permits UserCommand, ConfirmCommand, CancelCommand, AckCommand, PullCommand {

    /** 消息类型。 */
    String type();
}
