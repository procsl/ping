package cn.procsl.ping.boot.ai.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.tool.function.FunctionToolCallback;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 内置工具最小集：读文件与检索。
 *
 * <p>输入用 record 描述，由 Spring AI 负责 JSON ↔ 对象转换。</p>
 */
public final class BuiltinTools {

    private static final int MAX_FILE_BYTES = 1024 * 1024;
    private static final int MAX_MATCHES = 200;

    private BuiltinTools() {
    }

    /**
     * 构建内置工具注册表。
     *
     * @return 含 {@code read_file} 与 {@code grep} 的注册表
     */
    public static ToolRegistry registry() {
        ToolRegistry registry = new ToolRegistry();
        registry.register(new ToolDescriptor("read_file", "读取文本文件内容"),
                FunctionToolCallback.<ReadFileInput, String>builder("read_file", BuiltinTools::readFile)
                        .description("读取文本文件内容")
                        .inputType(ReadFileInput.class)
                        .build());
        registry.register(new ToolDescriptor("grep", "在目录中按正则检索文本"),
                FunctionToolCallback.<GrepInput, String>builder("grep", BuiltinTools::grep)
                        .description("在目录中按正则检索文本，返回 文件:行号:内容")
                        .inputType(GrepInput.class)
                        .build());
        return registry;
    }

    private static String readFile(ReadFileInput input) {
        try {
            byte[] bytes = Files.readAllBytes(Path.of(input.path()));
            if (bytes.length > MAX_FILE_BYTES) {
                return new String(bytes, 0, MAX_FILE_BYTES) + "\n[已截断]";
            }
            return new String(bytes);
        } catch (IOException e) {
            return "读取失败: " + e.getMessage();
        }
    }

    private static String grep(GrepInput input) {
        int limit = input.maxResults() == null || input.maxResults() <= 0 ? MAX_MATCHES : input.maxResults();
        List<String> matches = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(Path.of(input.path()))) {
            for (Path file : stream.filter(Files::isRegularFile).toList()) {
                if (matches.size() >= limit) {
                    break;
                }
                grepFile(file, input.pattern(), matches, limit);
            }
        } catch (Exception e) {
            return "检索失败: " + e.getMessage();
        }
        return matches.isEmpty() ? "无匹配" : String.join("\n", matches);
    }

    private static void grepFile(Path file, String pattern, List<String> matches, int limit) {
        try {
            List<String> lines = Files.readAllLines(file);
            for (int i = 0; i < lines.size() && matches.size() < limit; i++) {
                if (lines.get(i).matches(".*" + pattern + ".*")) {
                    matches.add(file + ":" + (i + 1) + ": " + lines.get(i));
                }
            }
        } catch (Exception e) {
            // 二进制或不可读文件跳过
        }
    }

    /**
     * {@code read_file} 输入。
     *
     * @param path 文件路径
     */
    public record ReadFileInput(String path) {
    }

    /**
     * {@code grep} 输入。
     *
     * @param pattern    正则
     * @param path       目录或文件路径
     * @param maxResults 最大匹配数
     */
    public record GrepInput(String pattern, String path,
                            @JsonProperty("max_results") Integer maxResults) {
    }
}
