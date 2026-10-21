package cn.procsl.ping.boot.ui.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import tools.jackson.databind.JsonNode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 统一节点（组合类型）。app/page/function 只是 type 值不同，不引入继承层次——组合优于继承。
 * 不可变（final 字段）：改字段 = 重建节点，编译期以纯函数式变换返回新树。
 *
 * <p>序列化形态：NON_EMPTY——{@code properties} 为 null、{@code children} 为空时不输出键，
 * function 产物即 {@code {type, id, properties}}，与 spec 编译产物形态一致。
 *
 * <p>标记 {@link Serializable}：防将来接入其他框架（远程调用/会话缓存等）时
 * 因未实现序列化接口而失效；字段均为可序列化类型（String、JsonNode、List）。
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Component implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 节点类型标识："app" | "page" | "function"
     */
    @Schema(description = "节点类型标识", allowableValues = {"app", "page", "function"})
    private final String type;

    /**
     * 节点标识：page.id 全局唯一；function.id 在 page 内唯一
     */
    @Schema(description = "节点标识：page.id 全局唯一；function.id 在其 page 内唯一")
    private final String id;

    /**
     * 节点属性：function 为 openapi operation 自包含描述（编译期内联），page/app 可为空
     */
    @Schema(description = "节点属性：function 为 openapi operation 自包含内联结果"
        + "（method/path/parameters/requestBody/responses-2xx + route），page/app 可为空")
    private final JsonNode properties;

    /**
     * 子节点：app > page > function；function 无子节点
     */
    @Schema(description = "子节点：app > page > function；function 无子节点")
    private final List<Component> children;

    public Component(String type, String id, JsonNode properties, List<Component> children) {
        this.type = type;
        this.id = id;
        this.properties = properties == null ? null : properties.deepCopy();
        this.children = children == null ? List.of() : List.copyOf(children);
    }

    /**
     * 按 type 分派的判断入口：不为 app/page/function 各建子类，只按 type 值区分约束。
     */
    public boolean isType(String expected) {
        return this.type != null && this.type.equals(expected);
    }
}
