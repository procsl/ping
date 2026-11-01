package cn.procsl.ping.boot.ai.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 三段式工具选择：会话指定 → 角色默认 → 工具服务清单。
 */
public final class ToolResolver {

    private static final Logger log = LoggerFactory.getLogger(ToolResolver.class);

    private final ToolService service;

    public ToolResolver(ToolService service) {
        this.service = service;
    }

    /**
     * 解析本次会话可用的工具。
     *
     * @param sessionTools 会话级指定，可为空
     * @param roleTools    角色默认，可为空
     * @return 工具描述（顺序稳定）
     */
    public List<ToolDescriptor> resolve(List<String> sessionTools, List<String> roleTools) {
        List<String> selected = sessionTools == null || sessionTools.isEmpty() ? roleTools : sessionTools;
        if (selected == null || selected.isEmpty()) {
            return List.of();
        }
        List<ToolDescriptor> descriptors = service.query(new ToolQuery(selected));
        if (descriptors.size() != selected.size()) {
            log.warn("部分工具名未注册，已忽略: {}", selected);
        }
        return descriptors;
    }
}
