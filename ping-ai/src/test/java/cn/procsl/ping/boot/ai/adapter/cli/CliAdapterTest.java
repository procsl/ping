package cn.procsl.ping.boot.ai.adapter.cli;

import cn.procsl.ping.boot.ai.core.Box;
import cn.procsl.ping.boot.ai.core.Session;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CliAdapterTest {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private CliAdapter adapter(boolean verbose) {
        return new CliAdapter(
                new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8),
                verbose);
    }

    private String stdout() {
        return out.toString(StandardCharsets.UTF_8);
    }

    private String stderr() {
        return err.toString(StandardCharsets.UTF_8);
    }

    @Test
    void 按对象_id_升序渲染且文本片段拼接() {
        List<Box> boxes = List.of(
                new Box(Session.TYPE_TEXT, 1, 1, "好"),
                new Box(Session.TYPE_TEXT, 1, 0, "你"),
                new Box(Session.TYPE_USER, 0, 0, "hi"),
                new Box(Session.TYPE_TEXT, 1, Box.END_SEQ, ""),
                new Box(Session.TYPE_USER, 0, Box.END_SEQ, ""));

        int exit = adapter(false).render(boxes);

        assertThat(exit).isZero();
        assertThat(stdout()).isEqualTo("hi\n你好\n");
        assertThat(stderr()).isEmpty();
    }

    @Test
    void 错误对象写标准错误并返回非零() {
        int exit = adapter(false).render(List.of(
                new Box(Session.TYPE_ERROR, 0, 0, "{\"code\":\"internal\"}"),
                new Box(Session.TYPE_ERROR, 0, Box.END_SEQ, "")));

        assertThat(exit).isEqualTo(CliAdapter.EXIT_MODEL_FAILURE);
        assertThat(stdout()).isEmpty();
        assertThat(stderr()).contains("internal");
    }

    @Test
    void 未知类型被忽略() {
        int exit = adapter(false).render(List.of(
                new Box("weird", 0, 0, "x"),
                new Box("weird", 0, Box.END_SEQ, "")));

        assertThat(exit).isZero();
        assertThat(stdout()).isEmpty();
        assertThat(stderr()).isEmpty();
    }

    @Test
    void 标准输出不含控制序列() {
        adapter(false).render(List.of(
                new Box(Session.TYPE_TEXT, 0, 0, "你好"),
                new Box(Session.TYPE_TEXT, 0, Box.END_SEQ, "")));

        assertThat(stdout()).doesNotContain("\u001b");
    }

    @Test
    void 工具对象仅在详细模式下写标准错误() {
        List<Box> boxes = List.of(
                new Box("tool", 0, 0, "read_file"),
                new Box("tool", 0, Box.END_SEQ, ""));

        adapter(false).render(boxes);
        assertThat(stderr()).isEmpty();

        adapter(true).render(boxes);
        assertThat(stderr()).contains("read_file");
        assertThat(stdout()).isEmpty();
    }
}
