package cn.procsl.ping.boot.ai.hook;

/**
 * hook 返回的执行策略。
 */
public enum Decision {
    /** 通过。 */
    PASS,
    /** 拒绝并短路。 */
    REJECT,
    /** 挂起等待外部输入（仅部分阶段有效）。 */
    WAIT,
    /** 替换内容（仅允许替换的阶段有效）。 */
    REPLACE
}
