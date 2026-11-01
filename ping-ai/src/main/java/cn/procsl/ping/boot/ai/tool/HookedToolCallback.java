package cn.procsl.ping.boot.ai.tool;

import cn.procsl.ping.boot.ai.hook.HookChain;
import cn.procsl.ping.boot.ai.hook.HookContext;
import cn.procsl.ping.boot.ai.hook.HookResult;
import cn.procsl.ping.boot.ai.hook.ToolWhitelistHook;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * 包在工具执行对象外的 hook 层。
 *
 * <p>工具由 Spring AI 内置循环调用；本类只负责：
 * 调用前校验、调用后校验、结果截断、以及把工具过程以可渲染对象投递。</p>
 */
public final class HookedToolCallback implements ToolCallback {

    private final ToolCallback delegate;
    private final String name;
    private final HookChain hooks;
    private final HookContext context;
    private final ToolEmitter emitter;
    private final ToolTruncator truncator;

    public HookedToolCallback(ToolCallback delegate, String name, HookChain hooks,
                              HookContext context, ToolEmitter emitter, ToolTruncator truncator) {
        this.delegate = delegate;
        this.name = name;
        this.hooks = hooks;
        this.context = context;
        this.emitter = emitter;
        this.truncator = truncator;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public String call(String toolInput) {
        return call(toolInput, null);
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        int objectId = emitter.begin(name, toolInput);

        HookResult before = hooks.run(ToolWhitelistHook.TOOL_BEFORE, context,
                new ToolWhitelistHook.ToolCallData(name, toolInput));
        if (before.rejected() || before.decision() == cn.procsl.ping.boot.ai.hook.Decision.WAIT) {
            String reason = String.valueOf(before.data());
            emitter.end(objectId, "denied", reason);
            return result("denied", reason);
        }

        String output;
        try {
            output = delegate.call(toolInput, toolContext);
        } catch (Exception e) {
            emitter.end(objectId, "failed", e.getMessage());
            return result("failed", e.getMessage());
        }

        HookResult after = hooks.run(ToolWhitelistHook.TOOL_AFTER, context,
                new ToolWhitelistHook.ToolResultData(name, output));
        if (after.rejected()) {
            String reason = String.valueOf(after.data());
            emitter.end(objectId, "failed", reason);
            return result("failed", reason);
        }

        String truncated = truncator.truncate(output);
        emitter.end(objectId, "ok", truncator.summary(truncated));
        return truncated;
    }

    private static String result(String status, String reason) {
        String safe = reason == null ? "" : reason.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"status\":\"" + status + "\",\"reason\":\"" + safe + "\"}";
    }
}
