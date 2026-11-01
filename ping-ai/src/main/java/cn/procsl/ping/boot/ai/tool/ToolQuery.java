package cn.procsl.ping.boot.ai.tool;

import java.util.List;

/**
 * 工具查询条件。
 *
 * @param names 需要的工具名；为空表示全部
 */
public record ToolQuery(List<String> names) {

    public ToolQuery {
        names = names == null ? List.of() : List.copyOf(names);
    }

    public static ToolQuery all() {
        return new ToolQuery(List.of());
    }
}
