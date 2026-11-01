package cn.procsl.ping.boot.ai.hook;

/**
 * hook 执行结果。
 *
 * @param decision 执行策略
 * @param data     替换后的数据；未替换时为原始数据
 */
public record HookResult(Decision decision, Object data) {

    public static HookResult pass(Object data) {
        return new HookResult(Decision.PASS, data);
    }

    public static HookResult reject(Object data) {
        return new HookResult(Decision.REJECT, data);
    }

    public static HookResult waitFor(Object data) {
        return new HookResult(Decision.WAIT, data);
    }

    public static HookResult replace(Object data) {
        return new HookResult(Decision.REPLACE, data);
    }

    public boolean rejected() {
        return decision == Decision.REJECT;
    }
}
