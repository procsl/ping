package cn.procsl.ping.boot.ai.adapter.cli;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CliArgumentsTest {

    @Test
    void 解析空格分隔与重复参数() {
        CliArguments args = CliArguments.parse(new String[]{
                "--role", "dev",
                "--prompt", "你好",
                "--image", "a.png",
                "--image", "b.png",
                "--tools", "read_file,grep",
                "--session", "abc123",
                "--verbose"});

        assertThat(args.role()).isEqualTo("dev");
        assertThat(args.prompt()).isEqualTo("你好");
        assertThat(args.images()).containsExactly("a.png", "b.png");
        assertThat(args.tools()).containsExactly("read_file", "grep");
        assertThat(args.session()).isEqualTo("abc123");
        assertThat(args.verbose()).isTrue();
        assertThat(args.validate()).isEmpty();
    }

    @Test
    void 解析等号写法() {
        CliArguments args = CliArguments.parse(new String[]{"--role=dev", "--prompt=hi"});

        assertThat(args.role()).isEqualTo("dev");
        assertThat(args.prompt()).isEqualTo("hi");
    }

    @Test
    void 未知参数被拒绝() {
        assertThatThrownBy(() -> CliArguments.parse(new String[]{"--role", "dev", "--nope", "1"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("--nope");
    }

    @Test
    void 缺少角色与非法会话标识() {
        assertThat(CliArguments.parse(new String[]{}).validate())
                .anyMatch(message -> message.contains("--role"));

        assertThat(CliArguments.parse(new String[]{"--role", "dev", "--session", "../etc"}).validate())
                .anyMatch(message -> message.contains("会话标识"));
    }

    @Test
    void 会话标识只允许字母数字() {
        assertThat(CliArguments.parse(new String[]{"--role", "dev", "--session", "20261101153045123ABCDEFGHIJ"})
                .validate()).isEmpty();
    }
}
