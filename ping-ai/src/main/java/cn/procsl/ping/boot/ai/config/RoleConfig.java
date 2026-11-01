package cn.procsl.ping.boot.ai.config;

import java.nio.file.Path;
import java.util.List;

/**
 * 角色运行配置。
 *
 * @param name         角色名（已通过校验）
 * @param model        引用的模型别名；为空表示使用全局默认
 * @param tools        角色创建时选定的工具集
 * @param systemPrompt 系统提示词；缺失时为 {@code null}
 * @param firstPrompt  默认用户提示词；缺失时为 {@code null}
 * @param directory    角色目录
 */
public record RoleConfig(
        String name,
        String model,
        List<String> tools,
        String systemPrompt,
        String firstPrompt,
        Path directory) {

    public RoleConfig {
        tools = tools == null ? List.of() : List.copyOf(tools);
    }
}
