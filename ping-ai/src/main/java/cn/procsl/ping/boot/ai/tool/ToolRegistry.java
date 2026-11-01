package cn.procsl.ping.boot.ai.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 代码内工具注册表。
 *
 * <p>工具名限定 {@code ^[a-z][a-z0-9_]{1,63}$}；清单外的名字在解析阶段告警并忽略。</p>
 */
public final class ToolRegistry implements ToolService {

    private static final Logger log = LoggerFactory.getLogger(ToolRegistry.class);

    /** 工具名合法模式。 */
    public static final Pattern NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9_]{1,63}$");

    private final Map<String, ToolDescriptor> descriptors = new LinkedHashMap<>();
    private final Map<String, ToolCallback> callbacks = new LinkedHashMap<>();

    /**
     * 注册一个工具。
     *
     * @param descriptor 工具描述
     * @param callback   执行对象
     */
    public ToolRegistry register(ToolDescriptor descriptor, ToolCallback callback) {
        if (!NAME_PATTERN.matcher(descriptor.name()).matches()) {
            throw new IllegalArgumentException("非法工具名: " + descriptor.name());
        }
        descriptors.put(descriptor.name(), descriptor);
        callbacks.put(descriptor.name(), callback);
        return this;
    }

    @Override
    public List<ToolDescriptor> query(ToolQuery query) {
        if (query == null || query.names().isEmpty()) {
            return List.copyOf(descriptors.values());
        }
        List<ToolDescriptor> result = new ArrayList<>();
        for (String name : query.names()) {
            ToolDescriptor descriptor = descriptors.get(name);
            if (descriptor == null) {
                log.warn("忽略未注册的工具: {}", name);
            } else {
                result.add(descriptor);
            }
        }
        return List.copyOf(result);
    }

    @Override
    public ToolCallback find(String name) {
        return callbacks.get(name);
    }
}
