package cn.procsl.ping.boot.ai.hook;

import java.util.List;

/**
 * hook 注册链：按注册顺序执行，任一拒绝即短路，替换结果传给下一个。
 */
public final class HookChain {

    private final List<Hook> hooks;

    public HookChain(List<Hook> hooks) {
        this.hooks = hooks == null ? List.of() : List.copyOf(hooks);
    }

    public static HookChain empty() {
        return new HookChain(List.of());
    }

    /**
     * 执行一个阶段。
     *
     * @param stage 阶段名
     * @param ctx   上下文
     * @param data  完整数据
     * @return 最终结果
     */
    public HookResult run(String stage, HookContext ctx, Object data) {
        Object current = data;
        for (Hook hook : hooks) {
            HookResult result = hook.apply(stage, ctx, current);
            if (result == null) {
                continue;
            }
            if (result.decision() == Decision.REJECT || result.decision() == Decision.WAIT) {
                return result;
            }
            if (result.decision() == Decision.REPLACE) {
                current = result.data();
            }
        }
        return HookResult.pass(current);
    }
}
