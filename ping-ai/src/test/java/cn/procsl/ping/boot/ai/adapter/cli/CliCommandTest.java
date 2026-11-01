package cn.procsl.ping.boot.ai.adapter.cli;

import cn.procsl.ping.boot.ai.core.ResolvedModel;
import cn.procsl.ping.boot.ai.support.StubChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.chat.client.ChatClient;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class CliCommandTest {

    @TempDir
    Path root;

    private final Function<ResolvedModel, ChatClient> stubFactory =
            model -> ChatClient.create(new StubChatModel("你", "好"));

    private void writeConfig() throws Exception {
        Files.writeString(root.resolve("config.json"), """
                {
                  "providers": {
                    "local": { "api_key": "k", "base_url": "http://localhost:1/v1" }
                  },
                  "models": {
                    "gpt": { "provider": "local", "model": "m" }
                  },
                  "default_model": "gpt"
                }
                """);
    }

    private void writeRole() throws Exception {
        Path dev = Files.createDirectories(root.resolve("roles/dev"));
        Files.writeString(dev.resolve("role.json"), "{ \"model\": \"gpt\", \"tools\": [] }");
        Files.writeString(dev.resolve("first.md"), "默认提示");
    }

    private Result execute(String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        CliCommand command = new CliCommand(
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8),
                stubFactory);
        int code = command.execute(CliArguments.parse(args));
        return new Result(code, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
    }

    @Test
    void 一条提示词进一段文本出并以零退出() throws Exception {
        writeConfig();
        writeRole();

        Result result = execute("--role", "dev", "--config", root.toString(), "--prompt", "hi");

        assertThat(result.code()).isZero();
        assertThat(result.out()).isEqualTo("hi\n你好\n");
        assertThat(result.err()).isEmpty();
    }

    @Test
    void 未提供提示词时使用默认用户提示词() throws Exception {
        writeConfig();
        writeRole();

        Result result = execute("--role", "dev", "--config", root.toString());

        assertThat(result.code()).isZero();
        assertThat(result.out()).startsWith("默认提示\n");
    }

    @Test
    void 角色不存在返回三() throws Exception {
        writeConfig();
        writeRole();

        Result result = execute("--role", "missing", "--config", root.toString(), "--prompt", "hi");

        assertThat(result.code()).isEqualTo(CliCommand.EXIT_ROLE);
        assertThat(result.err()).contains("角色不存在");
    }

    @Test
    void 非法会话标识返回二() throws Exception {
        writeConfig();
        writeRole();

        Result result = execute("--role", "dev", "--config", root.toString(), "--session", "../etc");

        assertThat(result.code()).isEqualTo(CliCommand.EXIT_ARGUMENT);
    }

    @Test
    void 没有可用模型返回四() throws Exception {
        writeRole();

        Result result = execute("--role", "dev", "--config", root.toString(), "--prompt", "hi");

        assertThat(result.code()).isEqualTo(CliCommand.EXIT_CONFIG);
    }

    @Test
    void 生成的会话标识符合约定() {
        String id = CliCommand.newSessionId();

        assertThat(id).hasSize(27);
        assertThat(id).matches("^\\d{17}[A-Z]{10}$");
    }

    private record Result(int code, String out, String err) {
    }
}
