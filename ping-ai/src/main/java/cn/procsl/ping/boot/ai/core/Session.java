package cn.procsl.ping.boot.ai.core;

import cn.procsl.ping.boot.ai.session.MessageRecord;
import cn.procsl.ping.boot.ai.session.SessionFile;
import cn.procsl.ping.boot.ai.session.SessionStore;
import cn.procsl.ping.boot.ai.session.ToolCallRecord;
import cn.procsl.ping.boot.ai.tool.ToolEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 会话对象：状态与行为都在对象内，不持有线程。
 *
 * <p>所有入口方法都通过内部锁串行化；工具线程回调也应走同一入口。
 * 轮次状态由本对象维护，适配器不持有也不推断。</p>
 */
public final class Session implements ToolEmitter {

    private static final Logger log = LoggerFactory.getLogger(Session.class);

    /** 用户消息对象类型。 */
    public static final String TYPE_USER = "user";
    /** 助手文本对象类型。 */
    public static final String TYPE_TEXT = "text";
    /** 错误对象类型。 */
    public static final String TYPE_ERROR = "error";

    /** 流式落盘阈值：累计字符数。 */
    static final int FLUSH_CHARS = 500;
    /** 流式落盘阈值：时间间隔。 */
    static final Duration FLUSH_INTERVAL = Duration.ofSeconds(5);

    private final String sessionId;
    private final String role;
    private final String modelAlias;
    private final ChatClient chatClient;
    private final String systemPrompt;
    private final Consumer<Box> sink;
    private final SessionStore store;
    private final ImageStore imageStore;
    private final int maxHistory;

    private final ObjectTable objects = new ObjectTable();
    private final ObjectProjector projector = new ObjectProjector();
    private final List<MessageRecord> messages = new ArrayList<>();
    private final Map<String, Integer> requestIds = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition idle = lock.newCondition();

    private State state = State.IDLE;
    private Thread worker;
    private Completion active;
    private int pending;
    private int streamingAssistantIndex = -1;
    private boolean cancelled;
    private int lastAck = -1;
    private List<ToolCallback> toolCallbacks = List.of();
    private final tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();

    public Session(String sessionId, ChatClient chatClient, String systemPrompt, Consumer<Box> sink) {
        this(sessionId, null, null, chatClient, systemPrompt, sink, null, null, 40, null);
    }

    public Session(String sessionId,
                   String role,
                   String modelAlias,
                   ChatClient chatClient,
                   String systemPrompt,
                   Consumer<Box> sink,
                   SessionStore store,
                   ImageStore imageStore,
                   int maxHistory,
                   SessionFile history) {
        this.sessionId = sessionId;
        this.role = role;
        this.modelAlias = modelAlias;
        this.chatClient = chatClient;
        this.systemPrompt = systemPrompt;
        this.sink = sink == null ? box -> { } : sink;
        this.store = store;
        this.imageStore = imageStore;
        this.maxHistory = maxHistory <= 0 ? 40 : maxHistory;
        if (history != null) {
            this.messages.addAll(history.messages());
            this.objects.seed(projector.project(this.messages));
        }
    }

    /**
     * 绑定本次会话可用的工具。
     *
     * @param callbacks 工具执行对象
     * @return 本对象，便于链式调用
     */
    public Session withTools(List<ToolCallback> callbacks) {
        this.toolCallbacks = callbacks == null ? List.of() : List.copyOf(callbacks);
        return this;
    }

    @Override
    public int begin(String name, String arguments) {
        int id;
        Box box;
        lock.lock();
        try {
            id = objects.start("tool");
            box = objects.push(id, toolContent(name, arguments));
            recordToolCall(name, arguments);
            persist();
        } finally {
            lock.unlock();
        }
        emit(active, box);
        return id;
    }

    @Override
    public void end(int id, String status, String summary) {
        Box content;
        Box end;
        lock.lock();
        try {
            content = objects.push(id, statusContent(status, summary));
            end = objects.finish(id);
            persist();
        } finally {
            lock.unlock();
        }
        emit(active, content);
        emit(active, end);
    }

    /**
     * 提交一条用户消息，立即返回完成对象。
     *
     * @param message 用户消息
     * @return 本轮 box 流
     */
    public Completion submit(UserMessage message) {
        AtomicReference<Completion> ref = new AtomicReference<>();
        List<Box> immediate = new ArrayList<>();
        boolean started = false;
        lock.lock();
        try {
            Completion completion = new Completion(this::cancel);
            ref.set(completion);
            if (state == State.RUNNING) {
                immediate.addAll(errorObject(CODE_BUSY, STATUS_RETRY));
            } else if (message == null || message.isEmpty()) {
                immediate.addAll(errorObject(CODE_INVALID_INPUT, STATUS_RETRY));
            } else {
                Integer existing = message.requestId() == null ? null : requestIds.get(message.requestId());
                if (existing != null) {
                    objects.history(existing).stream()
                            .filter(box -> box.id() == existing)
                            .forEach(immediate::add);
                } else {
                    List<Media> stored;
                    try {
                        stored = storeImages(message);
                    } catch (IllegalArgumentException e) {
                        stored = null;
                    }
                    if (stored == null) {
                        immediate.addAll(errorObject(CODE_INVALID_INPUT, STATUS_RETRY));
                    } else {
                        List<Media> media = stored;
                        String display = message.displayText();
                        messages.add(MessageRecord.user(display));
                        List<Box> userBoxes = objects.complete(TYPE_USER, display);
                        immediate.addAll(userBoxes);
                        if (message.requestId() != null) {
                            requestIds.put(message.requestId(), userBoxes.getFirst().id());
                        }
                        markErrorsHandled();
                        persist();
                        state = State.RUNNING;
                        cancelled = false;
                        pending = 1;
                        active = completion;
                        started = true;
                        worker = Thread.ofVirtual()
                                .name("ping-ai-round-" + sessionId)
                                .start(() -> runRound(completion, display, media));
                    }
                }
            }
        } finally {
            lock.unlock();
        }

        Completion completion = ref.get();
        immediate.forEach(box -> emit(completion, box));
        if (!started) {
            completion.finish();
        }
        return completion;
    }

    /**
     * 取消当前轮次：给未结束对象补结束 box，回到空闲。
     */
    public void cancel() {
        Thread current;
        Completion currentCompletion;
        List<Box> ends = new ArrayList<>();
        lock.lock();
        try {
            current = worker;
            currentCompletion = active;
            if (state != State.RUNNING) {
                return;
            }
            cancelled = true;
            markInterrupted();
            for (int id : objects.openIds()) {
                ends.add(objects.finish(id));
            }
            state = State.IDLE;
            worker = null;
            active = null;
            streamingAssistantIndex = -1;
            idle.signalAll();
            persist();
        } finally {
            lock.unlock();
        }
        if (current != null) {
            current.interrupt();
        }
        ends.forEach(box -> emit(currentCompletion, box));
        if (currentCompletion != null) {
            currentCompletion.finish();
        }
    }

    /**
     * 按 id 升序重放历史，每个对象两条 box。
     *
     * @param fromId 起始对象 id（含）
     * @return 重放序列
     */
    public List<Box> history(int fromId) {
        lock.lock();
        try {
            return objects.history(fromId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 记录投递进度（仅用于观测，不做断点续传）。
     *
     * @param id 已收到的对象 id
     */
    public void ack(int id) {
        lock.lock();
        try {
            lastAck = Math.max(lastAck, id);
        } finally {
            lock.unlock();
        }
    }

    /** 最近一次确认的对象 id。 */
    public int lastAck() {
        lock.lock();
        try {
            return lastAck;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 工具线程回调入口：一个工具调用处理完毕。
     */
    public void onToolCompleted() {
        Completion completion;
        boolean done;
        lock.lock();
        try {
            pending--;
            done = pending <= 0;
            completion = active;
            if (done) {
                state = State.IDLE;
                worker = null;
                active = null;
                idle.signalAll();
            }
        } finally {
            lock.unlock();
        }
        if (done && completion != null) {
            completion.finish();
        }
    }

    /**
     * 等待整轮结束。
     *
     * @param timeout 最长等待时间
     * @return 是否已空闲
     */
    public boolean awaitIdle(Duration timeout) {
        lock.lock();
        try {
            long nanos = timeout == null || timeout.isNegative() ? 0 : timeout.toNanos();
            while (state == State.RUNNING) {
                if (timeout == null || timeout.isNegative()) {
                    idle.await();
                } else if (nanos <= 0) {
                    return false;
                } else {
                    nanos = idle.awaitNanos(nanos);
                }
            }
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            lock.unlock();
        }
    }

    public State state() {
        lock.lock();
        try {
            return state;
        } finally {
            lock.unlock();
        }
    }

    public String sessionId() {
        return sessionId;
    }

    /** 消息表快照。 */
    public List<MessageRecord> messages() {
        lock.lock();
        try {
            return List.copyOf(messages);
        } finally {
            lock.unlock();
        }
    }

    /** 对象表快照（按 id 升序）。 */
    public List<ObjectTable.Entry> objects() {
        lock.lock();
        try {
            return objects.snapshot();
        } finally {
            lock.unlock();
        }
    }

    private void runRound(Completion completion, String display, List<Media> media) {
        int textId = -1;
        boolean done = false;
        try {
            int assistantIndex;
            lock.lock();
            try {
                assistantIndex = messages.size();
                messages.add(MessageRecord.assistant("").withInterrupted(true));
                streamingAssistantIndex = assistantIndex;
                textId = objects.start(TYPE_TEXT);
            } finally {
                lock.unlock();
            }

            ChatClient.ChatClientRequestSpec request = chatClient.prompt();
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                request = request.system(systemPrompt);
            }
            request = request.messages(promptMessages(display, media));
            if (!toolCallbacks.isEmpty()) {
                request = request.toolCallbacks(toolCallbacks);
            }

            Flux<String> content = request.stream().content();
            StringBuilder text = new StringBuilder();
            int sinceFlush = 0;
            long lastFlush = System.nanoTime();
            int id = textId;
            for (String fragment : content.toIterable()) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                text.append(fragment);
                emit(completion, locked(() -> objects.push(id, fragment)));
                sinceFlush += fragment.length();
                long now = System.nanoTime();
                if (sinceFlush >= FLUSH_CHARS || now - lastFlush >= FLUSH_INTERVAL.toNanos()) {
                    updateAssistant(assistantIndex, text.toString(), true);
                    sinceFlush = 0;
                    lastFlush = now;
                }
            }
            if (Thread.currentThread().isInterrupted()) {
                endIfOpen(completion, textId);
                lock.lock();
                try {
                    updateAssistantLocked(assistantIndex, text.toString(), true);
                } finally {
                    lock.unlock();
                }
            } else {
                int finished = textId;
                emit(completion, locked(() -> objects.finish(finished)));
                updateAssistant(assistantIndex, text.toString(), false);
            }
            done = true;
        } catch (Exception e) {
            if (cancelled || Thread.currentThread().isInterrupted()) {
                log.debug("轮次已取消: {}", e.getMessage());
                endIfOpen(completion, textId);
            } else {
                log.warn("模型调用失败: {}", e.getMessage());
                endIfOpen(completion, textId);
                locked(() -> {
                    streamingAssistantIndex = -1;
                    messages.add(MessageRecord.error(CODE_INTERNAL, STATUS_FATAL));
                    List<Box> boxes = objects.complete(TYPE_ERROR, error(CODE_INTERNAL, STATUS_FATAL));
                    persist();
                    return boxes;
                }).forEach(box -> emit(completion, box));
            }
            done = true;
        } finally {
            if (done) {
                finishStep(completion);
            }
        }
    }

    private void finishStep(Completion completion) {
        Completion finished;
        lock.lock();
        try {
            streamingAssistantIndex = -1;
            pending--;
            if (pending > 0) {
                return;
            }
            state = State.IDLE;
            worker = null;
            active = null;
            idle.signalAll();
            persist();
            finished = completion;
        } finally {
            lock.unlock();
        }
        finished.finish();
    }

    private List<Message> promptMessages(String currentText, List<Media> media) {
        List<MessageRecord> window;
        lock.lock();
        try {
            int end = messages.size();
            if (end > 0) {
                MessageRecord last = messages.get(end - 1);
                if (MessageRecord.ASSISTANT.equals(last.type()) && (last.text() == null || last.text().isEmpty())) {
                    end--;
                }
            }
            int from = Math.max(0, end - maxHistory);
            window = List.copyOf(messages.subList(from, end));
        } finally {
            lock.unlock();
        }
        List<Message> result = new ArrayList<>();
        int lastUser = -1;
        for (int i = 0; i < window.size(); i++) {
            if (MessageRecord.USER.equals(window.get(i).type())) {
                lastUser = i;
            }
        }
        for (int i = 0; i < window.size(); i++) {
            MessageRecord record = window.get(i);
            switch (record.type()) {
                case MessageRecord.USER -> {
                    if (i == lastUser && !media.isEmpty()) {
                        result.add(org.springframework.ai.chat.messages.UserMessage.builder().text(currentText).media(media).build());
                    } else {
                        result.add(new org.springframework.ai.chat.messages.UserMessage(record.text()));
                    }
                }
                case MessageRecord.ASSISTANT -> {
                    if (record.text() != null && !record.text().isBlank()) {
                        result.add(new AssistantMessage(record.text()));
                    }
                }
                default -> {
                    // system 单独传入；error 不进模型；tool 结果由工具循环处理
                }
            }
        }
        if (result.isEmpty()) {
            result.add(new org.springframework.ai.chat.messages.UserMessage(currentText));
        }
        return result;
    }

    private List<Media> storeImages(UserMessage message) {
        if (imageStore == null || message.images().isEmpty()) {
            return List.of();
        }
        List<Media> media = new ArrayList<>();
        for (String image : message.images()) {
            try {
                ImageStore.StoredImage stored = imageStore.store(Path.of(image));
                media.add(new Media(mimeType(stored.name()), new ByteArrayResource(stored.bytes())));
            } catch (Exception e) {
                throw new IllegalArgumentException("图片不可读: " + image, e);
            }
        }
        return media;
    }

    private static MimeType mimeType(String name) {
        String lower = name.toLowerCase(java.util.Locale.ROOT);
        if (lower.endsWith(".png")) {
            return MimeTypeUtils.IMAGE_PNG;
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MimeTypeUtils.IMAGE_JPEG;
        }
        if (lower.endsWith(".gif")) {
            return MimeTypeUtils.IMAGE_GIF;
        }
        if (lower.endsWith(".webp")) {
            return MimeTypeUtils.parseMimeType("image/webp");
        }
        return MimeTypeUtils.APPLICATION_OCTET_STREAM;
    }

    private void updateAssistant(int index, String text, boolean interrupted) {
        lock.lock();
        try {
            updateAssistantLocked(index, text, interrupted);
        } finally {
            lock.unlock();
        }
    }

    private void updateAssistantLocked(int index, String text, boolean interrupted) {
        if (index >= 0 && index < messages.size()) {
            messages.set(index, messages.get(index).withText(text).withInterrupted(interrupted));
        }
        persist();
    }

    private void markInterrupted() {
        if (streamingAssistantIndex >= 0 && streamingAssistantIndex < messages.size()) {
            messages.set(streamingAssistantIndex,
                    messages.get(streamingAssistantIndex).withInterrupted(true));
            streamingAssistantIndex = -1;
        }
    }

    private void markErrorsHandled() {
        boolean changed = false;
        for (int i = 0; i < messages.size(); i++) {
            MessageRecord record = messages.get(i);
            if (MessageRecord.ERROR.equals(record.type()) && !Boolean.TRUE.equals(record.handled())) {
                messages.set(i, record.withHandled(true));
                changed = true;
            }
        }
        if (changed) {
            objects.snapshot().stream()
                    .filter(entry -> TYPE_ERROR.equals(entry.type()) && !entry.handled())
                    .forEach(entry -> objects.markHandled(entry.id()));
        }
    }

    private void recordToolCall(String name, String arguments) {
        if (streamingAssistantIndex < 0 || streamingAssistantIndex >= messages.size()) {
            return;
        }
        MessageRecord record = messages.get(streamingAssistantIndex);
        List<ToolCallRecord> calls = new ArrayList<>(record.toolCalls());
        calls.add(new ToolCallRecord(UUID.randomUUID().toString(), name, parseArguments(arguments)));
        messages.set(streamingAssistantIndex, record.withToolCalls(calls));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseArguments(String arguments) {
        if (arguments == null || arguments.isBlank()) {
            return Map.of();
        }
        try {
            return mapper.readValue(arguments, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private String toolContent(String name, String arguments) {
        String args = arguments == null || arguments.isBlank() ? "{}" : arguments;
        return "{\"name\":\"" + name + "\",\"arguments\":" + args + "}";
    }

    private String statusContent(String status, String summary) {
        Map<String, Object> content = new java.util.LinkedHashMap<>();
        content.put("status", status);
        content.put("summary", summary == null ? "" : summary);
        return mapper.writeValueAsString(content);
    }

    private void persist() {
        if (store == null) {
            return;
        }
        store.save(new SessionFile(sessionId, role, modelAlias, List.copyOf(messages)));
    }

    private List<Box> errorObject(String code, String status) {
        List<Box> boxes = objects.complete(TYPE_ERROR, error(code, status));
        messages.add(MessageRecord.error(code, status));
        persist();
        return boxes;
    }

    private void endIfOpen(Completion completion, int id) {
        if (id < 0) {
            return;
        }
        Box box = locked(() -> objects.isOpen(id) ? objects.finish(id) : null);
        if (box != null) {
            emit(completion, box);
        }
    }

    private void emit(Completion completion, Box box) {
        if (box == null) {
            return;
        }
        sink.accept(box);
        if (completion != null) {
            completion.deliver(box);
        }
    }

    private <T> T locked(Supplier<T> action) {
        lock.lock();
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

    private static String error(String code, String status) {
        return "{\"code\":\"" + code + "\",\"status\":\"" + status + "\"}";
    }

    /** 轮次状态。 */
    public enum State {
        /** 空闲。 */
        IDLE,
        /** 正在响应。 */
        RUNNING
    }

    /** 错误码：上一轮未结束。 */
    public static final String CODE_BUSY = "busy";
    /** 错误码：提交内容不合法。 */
    public static final String CODE_INVALID_INPUT = "invalid_input";
    /** 错误码：未归类。 */
    public static final String CODE_INTERNAL = "internal";
    /** 处理方式：可重试。 */
    public static final String STATUS_RETRY = "retry";
    /** 处理方式：不可继续。 */
    public static final String STATUS_FATAL = "fatal";

    /**
     * 用户消息（上游输入）。
     *
     * @param text      文本，可为空
     * @param images    图片引用，可为空
     * @param requestId 可选请求标识，用于去重
     */
    public record UserMessage(String text, List<String> images, String requestId) {

        public UserMessage {
            images = images == null ? List.of() : List.copyOf(images);
        }

        public UserMessage(String text) {
            this(text, List.of(), null);
        }

        public UserMessage(String text, List<String> images) {
            this(text, images, null);
        }

        /** 文本与图片是否都为空。 */
        public boolean isEmpty() {
            return (text == null || text.isBlank()) && images.isEmpty();
        }

        /** 下游显示投影：文本 + 图片文件名。 */
        public String displayText() {
            StringBuilder builder = new StringBuilder(text == null ? "" : text);
            images.forEach(image -> {
                if (!builder.isEmpty()) {
                    builder.append('\n');
                }
                builder.append('[').append(image).append(']');
            });
            return builder.toString();
        }
    }
}
