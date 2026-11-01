package cn.procsl.ping.boot.ai.tool;

import cn.procsl.ping.boot.ai.hook.Decision;
import cn.procsl.ping.boot.ai.hook.HookChain;
import cn.procsl.ping.boot.ai.hook.HookContext;
import cn.procsl.ping.boot.ai.hook.HookResult;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HookedToolCallbackTest {

    private final List<String> events = new ArrayList<>();

    private ToolCallback delegate(String name, String output) {
        return new ToolCallback() {
            @Override
            public org.springframework.ai.tool.definition.ToolDefinition getToolDefinition() {
                return DefaultToolDefinition.builder().name(name).description(name).inputSchema("{}").build();
            }

            @Override
            public String call(String toolInput) {
                events.add("exec:" + toolInput);
                return output;
            }
        };
    }

    private ToolEmitter emitter() {
        return new ToolEmitter() {
            @Override
            public int begin(String name, String arguments) {
                events.add("begin:" + name + ":" + arguments);
                return 7;
            }

            @Override
            public void end(int id, String status, String summary) {
                events.add("end:" + id + ":" + status);
            }
        };
    }

    @Test
    void 通过时执行并投递对象() {
        HookChain chain = new HookChain(List.of(
                (stage, ctx, data) -> HookResult.pass(data)));
        HookedToolCallback callback = new HookedToolCallback(
                delegate("read_file", "内容"), "read_file", chain,
                new HookContext("s", "dev"), emitter(), new ToolTruncator());

        String result = callback.call("{}");

        assertThat(result).isEqualTo("内容");
        assertThat(events).containsExactly("begin:read_file:{}", "exec:{}", "end:7:ok");
    }

    @Test
    void 拒绝时不执行() {
        HookChain chain = new HookChain(List.of(
                (stage, ctx, data) -> HookResult.reject("no")));
        HookedToolCallback callback = new HookedToolCallback(
                delegate("read_file", "内容"), "read_file", chain,
                new HookContext("s", "dev"), emitter(), new ToolTruncator());

        String result = callback.call("{}");

        assertThat(result).contains("denied");
        assertThat(events).containsExactly("begin:read_file:{}", "end:7:denied");
    }

    @Test
    void 等待确认在非交互形态按拒绝处理() {
        HookChain chain = new HookChain(List.of(
                (stage, ctx, data) -> new HookResult(Decision.WAIT, "need confirm")));
        HookedToolCallback callback = new HookedToolCallback(
                delegate("read_file", "内容"), "read_file", chain,
                new HookContext("s", "dev"), emitter(), new ToolTruncator());

        assertThat(callback.call("{}")).contains("denied");
        assertThat(events).noneMatch(event -> event.startsWith("exec:"));
    }

    @Test
    void 执行异常被转换为失败结果() {
        ToolCallback broken = new ToolCallback() {
            @Override
            public org.springframework.ai.tool.definition.ToolDefinition getToolDefinition() {
                return DefaultToolDefinition.builder().name("read_file").description("x").inputSchema("{}").build();
            }

            @Override
            public String call(String toolInput) {
                throw new IllegalStateException("boom");
            }
        };
        HookedToolCallback callback = new HookedToolCallback(
                broken, "read_file", HookChain.empty(),
                new HookContext("s", "dev"), emitter(), new ToolTruncator());

        assertThat(callback.call("{}")).contains("failed");
        assertThat(events).contains("end:7:failed");
    }

    @Test
    void 超长结果被截断并带标记() {
        HookedToolCallback callback = new HookedToolCallback(
                delegate("read_file", "x".repeat(40_000)), "read_file", HookChain.empty(),
                new HookContext("s", "dev"), emitter(), new ToolTruncator());

        String result = callback.call("{}");

        assertThat(result).contains("已截断");
        assertThat(result.length()).isLessThan(40_000);
    }

    @Test
    void 白名单工具集之外的调用被拒绝() {
        HookChain chain = new HookChain(List.of(
                new cn.procsl.ping.boot.ai.hook.ToolWhitelistHook(Set.of("grep"))));
        HookedToolCallback callback = new HookedToolCallback(
                delegate("read_file", "内容"), "read_file", chain,
                new HookContext("s", "dev"), emitter(), new ToolTruncator());

        assertThat(callback.call("{}")).contains("denied");
    }
}
