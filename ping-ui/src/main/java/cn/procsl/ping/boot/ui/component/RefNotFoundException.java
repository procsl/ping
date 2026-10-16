package cn.procsl.ping.boot.ui.component;

/**
 * {@code ref} 断链异常。
 *
 * <p>编译阶段解析 input/output 组件的路径式 {@code ref} 时，若目标 operation 或字段
 * 不存在则抛出。异常信息包含完整 {@code ref} 路径，便于修正声明。
 *
 * @see RefResolver
 */
public class RefNotFoundException extends RefResolveException {

    private final String ref;

    public RefNotFoundException(String ref, String reason) {
        super("Ref '" + ref + "' not found: " + reason);
        this.ref = ref;
    }

    public String getRef() {
        return ref;
    }
}
