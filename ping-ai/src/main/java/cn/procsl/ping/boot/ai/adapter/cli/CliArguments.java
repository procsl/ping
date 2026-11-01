package cn.procsl.ping.boot.ai.adapter.cli;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 非交互式长参数。
 *
 * <p>只提供长参数（Spring Boot 原生风格），不提供短参数。
 * 支持 {@code --key=value} 与 {@code --key value} 两种写法，{@code --image} 可重复。</p>
 */
public record CliArguments(
        String role,
        String prompt,
        List<String> images,
        List<String> tools,
        String session,
        String model,
        String config,
        boolean verbose) {

    /** 会话标识合法模式。 */
    public static final Pattern SESSION_PATTERN = Pattern.compile("^[A-Za-z0-9]{1,64}$");

    private static final Set<String> KNOWN = Set.of(
            "role", "prompt", "image", "tools", "session", "model", "config", "verbose", "web");

    public CliArguments {
        images = images == null ? List.of() : List.copyOf(images);
        tools = tools == null ? List.of() : List.copyOf(tools);
    }

    /**
     * 解析命令行。
     *
     * @param args 原始参数
     * @return 解析结果
     * @throws IllegalArgumentException 未知参数
     */
    public static CliArguments parse(String[] args) {
        Map<String, List<String>> values = new LinkedHashMap<>();
        Set<String> flags = new LinkedHashSet<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("--")) {
                throw new IllegalArgumentException("不支持的参数: " + arg);
            }
            String body = arg.substring(2);
            String key;
            String value = null;
            int eq = body.indexOf('=');
            if (eq >= 0) {
                key = body.substring(0, eq);
                value = body.substring(eq + 1);
            } else {
                key = body;
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    value = args[++i];
                }
            }
            if (!KNOWN.contains(key)) {
                throw new IllegalArgumentException("未知参数: --" + key);
            }
            if (value == null) {
                flags.add(key);
            } else {
                values.computeIfAbsent(key, ignored -> new ArrayList<>()).add(value);
            }
        }
        return new CliArguments(
                first(values, "role"),
                first(values, "prompt"),
                values.getOrDefault("image", List.of()),
                splitTools(values.get("tools")),
                first(values, "session"),
                first(values, "model"),
                first(values, "config"),
                flags.contains("verbose"));
    }

    /**
     * 校验参数组合。
     *
     * @return 错误信息列表；为空表示通过
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        if (role == null || role.isBlank()) {
            errors.add("缺少必填参数 --role");
        }
        if (session != null && !SESSION_PATTERN.matcher(session).matches()) {
            errors.add("会话标识非法，仅允许字母与数字且不超过 64 字符: " + session);
        }
        return List.copyOf(errors);
    }

    private static String first(Map<String, List<String>> values, String key) {
        List<String> list = values.get(key);
        return list == null || list.isEmpty() ? null : list.getFirst();
    }

    private static List<String> splitTools(List<String> raw) {
        if (raw == null) {
            return List.of();
        }
        List<String> tools = new ArrayList<>();
        raw.forEach(value -> {
            for (String part : value.split(",")) {
                if (!part.isBlank()) {
                    tools.add(part.trim());
                }
            }
        });
        return tools;
    }
}
