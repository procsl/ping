package cn.procsl.ping.boot.ui.component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 抽象 UI 组件树聚合根。
 *
 * <p>承载所有模块的 compose.json 合并结果，包含 operation 和 function 两类子节点。
 */
@Getter
public class Compose {

    private final List<JsonNode> children;

    public Compose(ArrayNode children) {
        List<JsonNode> list = new ArrayList<>();
        children.forEach(list::add);
        this.children = Collections.unmodifiableList(list);
    }

}
