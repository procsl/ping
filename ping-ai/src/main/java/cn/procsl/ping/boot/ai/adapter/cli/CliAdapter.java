package cn.procsl.ping.boot.ai.adapter.cli;

import cn.procsl.ping.boot.ai.core.Box;
import cn.procsl.ping.boot.ai.core.Completion;
import cn.procsl.ping.boot.ai.core.Session;

import java.io.PrintStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 非交互式渲染：按对象 id 升序，用户消息与助手文本写标准输出，错误写标准错误。
 *
 * <p>不实现 ack / 重放 / 断线恢复——进程内直连，没有丢包与重连。</p>
 */
public final class CliAdapter {

    /** 退出码：模型调用失败。 */
    public static final int EXIT_MODEL_FAILURE = 5;

    private final PrintStream out;
    private final PrintStream err;
    private final boolean verbose;

    public CliAdapter(PrintStream out, PrintStream err, boolean verbose) {
        this.out = out;
        this.err = err;
        this.verbose = verbose;
    }

    /**
     * 提交并等待本轮结束，然后渲染。
     *
     * @param session 会话
     * @param prompt  用户提示词
     * @param images  图片引用
     * @param timeout 等待上限
     * @return 退出码
     */
    public int run(Session session, String prompt, List<String> images, Duration timeout) {
        List<Box> boxes = Collections.synchronizedList(new ArrayList<>());
        Completion completion = session.submit(new Session.UserMessage(prompt, images));
        completion.subscribe(boxes::add);
        completion.await(timeout);
        return render(boxes);
    }

    /**
     * 渲染一批 box。
     *
     * @param boxes 本轮全部 box
     * @return 退出码
     */
    public int render(List<Box> boxes) {
        Map<Integer, List<Box>> byId = new TreeMap<>();
        boxes.forEach(box -> byId.computeIfAbsent(box.id(), ignored -> new ArrayList<>()).add(box));

        int exitCode = 0;
        for (List<Box> group : byId.values()) {
            String type = group.getFirst().type();
            String content = group.stream()
                    .filter(box -> !box.isEnd())
                    .sorted(Comparator.comparingInt(Box::seq))
                    .map(Box::content)
                    .collect(Collectors.joining());
            boolean ended = group.stream().anyMatch(Box::isEnd);
            switch (type) {
                case Session.TYPE_USER, Session.TYPE_TEXT -> {
                    out.print(content);
                    if (ended) {
                        out.println();
                    }
                }
                case Session.TYPE_ERROR -> {
                    err.println(content);
                    exitCode = EXIT_MODEL_FAILURE;
                }
                case "tool" -> {
                    if (verbose) {
                        err.println("[tool] " + content);
                    }
                }
                default -> {
                    // 未知类型忽略：CLI 只实现自己认识的 type 分派表
                }
            }
        }
        out.flush();
        err.flush();
        return exitCode;
    }
}
