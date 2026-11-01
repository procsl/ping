package cn.procsl.ping.boot.ai.session;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SessionIdsTest {

    @Test
    void 生成时间戳加十位大写字母() {
        String id = SessionIds.generate();

        assertThat(id).hasSize(27);
        assertThat(id).matches("^\\d{17}[A-Z]{10}$");
        assertThat(SessionIds.isValid(id)).isTrue();
    }

    @Test
    void 校验拒绝路径分隔符与超长() {
        assertThat(SessionIds.isValid("abc123")).isTrue();
        assertThat(SessionIds.isValid("")).isFalse();
        assertThat(SessionIds.isValid(null)).isFalse();
        assertThat(SessionIds.isValid("../etc")).isFalse();
        assertThat(SessionIds.isValid("a".repeat(65))).isFalse();
    }
}
