# Box 契约

渲染数据只有一种形态：box。

```
Box = { type, id, seq, content }
```

| 字段 | 规则 |
|---|---|
| `type` | 开放字符串。协议不定义取值，消费端自行分派；同一对象内不变 |
| `id` | 对象 id，会话内从 0 递增、不复用；渲染顺序 = id 升序，id 0 恒在顶端 |
| `seq` | 对象内片段序号：`0` 开始、`>0` 追加；其余（负数、缺失、非法）一律表示该对象结束 |
| `content` | 字符串，不透明。结构化数据由后端序列化，消费端按 `type` 解释 |

`seq` 的解析只认非负整数：`null`、空串、非数字、负数都归一为结束（`-1`）。

## 对象

对象由同一 `id` 的 box 组成。

```
流式对象（助手文本）
  seq=0 → seq=1 → seq=2 → 结束

完整对象（用户消息、错误、历史重放）
  seq=0 带完整内容 → 结束
```

- 同一会话内允许多个对象同时推送，不同 `id` 的 box 可以交错到达。
- 对象之间只靠 `id` 排序，不靠到达顺序拼接。
- 新对象分配下一个 id；id 只增不复用。

### 示例

```
box(id=0, type=user, seq=0,  "帮我看看这个文件")
box(id=0, type=user, seq=-1, "")
box(id=1, type=text, seq=0,  "我先读一下")
box(id=2, type=tool, seq=0,  "{\"name\":\"read_file\",\"arguments\":{...}}")
box(id=1, type=text, seq=1,  "这个文件。")
box(id=2, type=tool, seq=-1, "")
box(id=1, type=text, seq=-1, "")
```

## 重放与确认

```
history()        从头重放
history(fromId)  从指定对象 id 开始重放
```

- 与实时推送同一套 box，唯一区别：每个对象只发两条 —— `seq=0` 带完整内容，`seq=-1` 结束。
- 重放沿用对象原本的 id，不重新编号。
- 每收到一个完整 box 回 `ack(id)`；同一 id 重复确认无副作用。
- 对象 id 或 `seq` 出现缺口 → `history(from=缺口)`。

## 断线

```
断线 → 服务端清连接；客户端清掉未结束对象与本地状态
重连 → 客户端全量 history()
```

前端不持久化状态：进程重启必然全量重放；同进程内重连可用 `history(fromId)`。

## 消费端状态机

```java
// objects: id -> {type, content, open, lastSeq}
// expected: 下一个新对象 id
void onBox(Box b) {
    if (b.id() > expected) { history(expected); return; }

    Obj existing = objects.get(b.id());
    if (b.id() < expected && existing != null && !existing.open()) return;

    ack(b.id());

    if (!b.isEnd()) {
        if (b.seq() == 0) {
            objects.put(b.id(), new Obj(b.type(), b.content()));
            expected = Math.max(expected, b.id() + 1);
        } else {
            if (existing == null || !existing.open() || b.seq() != existing.lastSeq + 1) {
                objects.remove(b.id());
                history(b.id());
                return;
            }
            existing.append(b.content());
            existing.lastSeq(b.seq());
        }
    } else if (existing != null) {
        existing.close();
    }
}
```

实现见 `cn.procsl.ping.boot.ai.core.BoxTracker`。

## 对象表与重建

对象表是渲染投影，**不落盘**。加载会话时由消息表重建：

| 消息 | 对象数 | 说明 |
|---|---|---|
| `user` | 1 | 一条用户消息一个对象 |
| `assistant`（纯文本） | 1 | 文本一个对象 |
| `assistant`（带 tool_calls） | 1 + N | 文本一个对象，每个 tool call 各自一个对象 |
| `tool` | 0 | 并入对应 tool call 对象 |
| `error` | 1 | 已处理的错误在重放时带 `handled` 标记 |
| `system` | 0 | 不进渲染 |

重建必须幂等：同一份消息表两次加载得到逐字段相同的对象表。
流式对象的最终内容必须等于全量重建的 `content`。

实现见 `cn.procsl.ping.boot.ai.core.ObjectProjector`。

## 实现约定的 type

以下取值属于实现约定，不属于协议：

| type | content |
|---|---|
| `user` | 用户输入文本（含图片文件名） |
| `text` | 助手文本片段 |
| `tool` | `{"name":..., "arguments":{...}}` |
| `error` | `{"code":..., "status":...}`；已处理时附 `"handled":true` |
