package cn.procsl.ping.boot.ai.session;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * 会话标识的生成与校验。
 *
 * <p>自动生成：{@code yyyyMMddHHmmssSSS} + 10 位大写随机字母（27 字符，无分隔符）。
 * 外部传入必须是字母数字组合，防止路径穿越。</p>
 */
public final class SessionIds {

    /** 会话标识合法模式。 */
    public static final Pattern PATTERN = Pattern.compile("^[A-Za-z0-9]{1,64}$");

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final int RANDOM_LENGTH = 10;

    private SessionIds() {
    }

    public static boolean isValid(String sessionId) {
        return sessionId != null && PATTERN.matcher(sessionId).matches();
    }

    /**
     * 生成会话标识。
     *
     * @return 27 字符标识
     */
    public static String generate() {
        StringBuilder builder = new StringBuilder(LocalDateTime.now().format(TIME));
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            builder.append((char) ('A' + random.nextInt(26)));
        }
        return builder.toString();
    }
}
