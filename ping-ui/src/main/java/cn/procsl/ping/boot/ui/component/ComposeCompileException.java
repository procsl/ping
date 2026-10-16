package cn.procsl.ping.boot.ui.component;

/**
 * 组件编译（组装）阶段声明校验失败的异常基类。
 *
 * <p>抽象 UI 框架在启动阶段编译所有 compose.json（design D6）：任何声明错误都应导致
 * 启动失败，跑出异常即暴露问题，不允许运行时降级。
 *
 * <p>失败语义按唯一继承层次分类，同一失败类型只有一个粗粒度类型：
 * <ul>
 *   <li>{@link ComposeParseException} —— compose.json 文件级非法（JSON 解析失败、顶层结构不合法）</li>
 *   <li>{@link RefNotFoundException} —— 引用断链（{@code openapi://} operation / 字段路径、
 *       OpenAPI schema 内部 {@code $ref} 的目标不存在）</li>
 *   <li>{@link CircularReferenceException} —— OpenAPI schema {@code $ref} 循环引用</li>
 *   <li>其余声明非法（引用格式错误、重复 operationId、无成功响应、api 标注缺失或非法、
 *       覆盖值类型不匹配等）直接抛本基类</li>
 * </ul>
 */
public class ComposeCompileException extends RuntimeException {

    public ComposeCompileException(String message) {
        super(message);
    }

    public ComposeCompileException(String message, Throwable cause) {
        super(message, cause);
    }
}
