package cn.procsl.ping.boot.ai.hook;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HookTest {

    @Test
    void 按注册顺序执行且替换结果传给下一个() {
        List<String> seen = new ArrayList<>();
        Hook first = (stage, ctx, data) -> {
            seen.add("first:" + data);
            return HookResult.replace(data + "-1");
        };
        Hook second = (stage, ctx, data) -> {
            seen.add("second:" + data);
            return HookResult.pass(data);
        };

        HookResult result = new HookChain(List.of(first, second)).run("s", null, "x");

        assertThat(seen).containsExactly("first:x", "second:x-1");
        assertThat(result.data()).isEqualTo("x-1");
    }

    @Test
    void 拒绝短路() {
        List<String> seen = new ArrayList<>();
        Hook first = (stage, ctx, data) -> {
            seen.add("first");
            return HookResult.reject("no");
        };
        Hook second = (stage, ctx, data) -> {
            seen.add("second");
            return HookResult.pass(data);
        };

        assertThat(new HookChain(List.of(first, second)).run("s", null, "x").rejected()).isTrue();
        assertThat(seen).containsExactly("first");
    }

    @Test
    void 白名单校验工具名与参数() {
        ToolWhitelistHook hook = new ToolWhitelistHook(Set.of("read_file"));

        assertThat(hook.apply(ToolWhitelistHook.TOOL_BEFORE, null,
                new ToolWhitelistHook.ToolCallData("read_file", "{}")).decision()).isEqualTo(Decision.PASS);
        assertThat(hook.apply(ToolWhitelistHook.TOOL_BEFORE, null,
                new ToolWhitelistHook.ToolCallData("shell", "{}")).rejected()).isTrue();
        assertThat(hook.apply(ToolWhitelistHook.TOOL_BEFORE, null,
                new ToolWhitelistHook.ToolCallData("read_file", "not json")).rejected()).isTrue();
        assertThat(hook.apply(ToolWhitelistHook.TOOL_AFTER, null,
                new ToolWhitelistHook.ToolResultData("read_file", "ok")).decision()).isEqualTo(Decision.PASS);
        assertThat(hook.apply(ToolWhitelistHook.TOOL_AFTER, null,
                new ToolWhitelistHook.ToolResultData("shell", "ok")).rejected()).isTrue();
    }
}
