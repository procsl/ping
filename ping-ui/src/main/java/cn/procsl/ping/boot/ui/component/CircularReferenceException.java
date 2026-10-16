package cn.procsl.ping.boot.ui.component;

import java.util.List;

/**
 * {@code $ref} 循环引用异常。
 *
 * <p>编译阶段展开 OpenAPI schema 的 {@code $ref} 时，若目标 schema 的展开路径中
 * 再次引用到正在解析的 schema，则判定为循环引用并抛出。
 *
 * <p>递归结构（如树形 {@code Node.child -> Node}）无法在编译期完全内联，
 * 因此这里选择 fail fast，要求后端补全或调整 schema。
 *
 * @see SchemaRefExpander
 */
public class CircularReferenceException extends ReferenceResolveException {

    private final String ref;
    private final List<String> resolutionPath;

    public CircularReferenceException(String ref, List<String> resolutionPath) {
        super("Circular $ref detected: '" + ref + "' (resolution path: " + resolutionPath + ")");
        this.ref = ref;
        this.resolutionPath = List.copyOf(resolutionPath);
    }

    public String getRef() {
        return ref;
    }

    public List<String> getResolutionPath() {
        return resolutionPath;
    }
}
