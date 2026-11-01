package cn.procsl.ping.boot.ai.tool;

import org.springframework.ai.tool.ToolCallback;

import java.util.List;

/**
 * 工具服务：统一管理已开发好的工具。
 */
public interface ToolService {

    /**
     * 查询可用工具。
     *
     * @param query 查询条件
     * @return 命中的工具描述
     */
    List<ToolDescriptor> query(ToolQuery query);

    /**
     * 按名称取执行对象。
     *
     * @param name 工具名
     * @return 执行对象；不存在时为 {@code null}
     */
    ToolCallback find(String name);
}
