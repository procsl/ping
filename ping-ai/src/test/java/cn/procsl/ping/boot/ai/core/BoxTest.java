package cn.procsl.ping.boot.ai.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BoxTest {

    @Test
    void 非负整数是片段序号() {
        assertThat(Box.parseSeq("0")).isZero();
        assertThat(Box.parseSeq("1")).isEqualTo(1);
        assertThat(Box.parseSeq(" 12 ")).isEqualTo(12);
    }

    @Test
    void 缺失非法与负数一律是结束() {
        assertThat(Box.parseSeq(null)).isEqualTo(Box.END_SEQ);
        assertThat(Box.parseSeq("")).isEqualTo(Box.END_SEQ);
        assertThat(Box.parseSeq("abc")).isEqualTo(Box.END_SEQ);
        assertThat(Box.parseSeq("-1")).isEqualTo(Box.END_SEQ);
        assertThat(Box.parseSeq("-99")).isEqualTo(Box.END_SEQ);
    }

    @Test
    void 结束片段判定() {
        assertThat(new Box("text", 1, 0, "a").isEnd()).isFalse();
        assertThat(new Box("text", 1, Box.END_SEQ, "").isEnd()).isTrue();
    }
}
