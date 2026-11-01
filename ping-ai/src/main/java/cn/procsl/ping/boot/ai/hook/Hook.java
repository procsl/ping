package cn.procsl.ping.boot.ai.hook;

/**
 * 阶段化 hook。
 *
 * <p>入参是阶段名 + context + 完整数据（流式阶段传入拼装完成的内容）。
 * hook 无状态、不依赖 Spring；具体阶段集合暂缓定义，机制先落地。</p>
 */
@FunctionalInterface
public interface Hook {

    /**
     * 处理一个阶段。
     *
     * @param stage 阶段名
     * @param ctx   上下文
     * @param data  完整数据
     * @return 执行策略
     */
    HookResult apply(String stage, HookContext ctx, Object data);
}
