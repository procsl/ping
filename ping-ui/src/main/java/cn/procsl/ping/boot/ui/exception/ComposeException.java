package cn.procsl.ping.boot.ui.exception;

/**
 * 编译期声明校验失败的异常基类。
 *
 * <p>编译在启动阶段完成（design D8）：任何结构性错误都导致启动失败，全有或全无，
 * 不允许运行时降级。报错信息 SHALL 含具体引用路径与声明来源。
 *
 * <p>失败类型收敛为两个（design D9，组合优于继承层次膨胀）：
 * <ul>
 *   <li>{@link ComposeParseException} —— compose.json 文件级非法（JSON 解析失败等）</li>
 *   <li>本基类 —— 其余编译错误：page.id / function.id 重复、operationId 断链与冲突、
 *       schema {@code $ref} 循环引用等</li>
 * </ul>
 */
public class ComposeException extends RuntimeException {

    public ComposeException(String message) {
        super(message);
    }

    public ComposeException(String message, Throwable cause) {
        super(message, cause);
    }
}
