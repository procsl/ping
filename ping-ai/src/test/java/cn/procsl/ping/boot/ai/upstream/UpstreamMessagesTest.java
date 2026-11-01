package cn.procsl.ping.boot.ai.upstream;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpstreamMessagesTest {

    @Test
    void 用户消息往返一致() {
        UpstreamMessage message = UpstreamMessages.parse("""
                { "type": "user", "text": "hi", "images": ["a.png"], "request_id": "r1" }
                """).orElseThrow();

        assertThat(message).isInstanceOf(UserCommand.class);
        UserCommand user = (UserCommand) message;
        assertThat(user.text()).isEqualTo("hi");
        assertThat(user.images()).containsExactly("a.png");
        assertThat(user.requestId()).isEqualTo("r1");
    }

    @Test
    void 确认消息() {
        UpstreamMessage message = UpstreamMessages.parse("""
                { "type": "confirm", "request_id": "c1", "decision": "allow" }
                """).orElseThrow();

        assertThat(message).isInstanceOf(ConfirmCommand.class);
        assertThat(((ConfirmCommand) message).decision()).isEqualTo("allow");
    }

    @Test
    void 取消消息没有额外字段() {
        assertThat(UpstreamMessages.parse("{ \"type\": \"cancel\" }").orElseThrow())
                .isInstanceOf(CancelCommand.class);
    }

    @Test
    void 确认与重放消息() {
        assertThat(((AckCommand) UpstreamMessages.parse("{ \"type\": \"ack\", \"id\": 3 }").orElseThrow()).id())
                .isEqualTo(3);
        PullCommand pull = (PullCommand) UpstreamMessages.parse("{ \"type\": \"pull\" }").orElseThrow();
        assertThat(pull.fromOrDefault()).isZero();
    }

    @Test
    void 未知类型被忽略() {
        assertThat(UpstreamMessages.parse("{ \"type\": \"nope\", \"x\": 1 }")).isEmpty();
        assertThat(UpstreamMessages.parse("not json")).isEmpty();
        assertThat(UpstreamMessages.parse(null)).isEmpty();
    }
}
