package cn.procsl.ping.boot.ai.hook;

/**
 * hook 上下文。
 *
 * @param sessionId 会话标识
 * @param role      角色名
 */
public record HookContext(String sessionId, String role) {
}
