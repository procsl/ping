/**
 * 消息通知机制 —— 对应 `ping-ui/doc/sdk-design.md` §4。
 *
 * SDK 只提供 `on` / `off` / `emit` 机制，不定义消息组件语义；
 * 消息内容的语义由使用方自行约定（SDK 只承载传递）。
 */

/** 事件回调：收到 emit 携带的数据 */
export type MessageCallback = (payload: unknown) => void

/**
 * 消息总线：注册 / 注销 / 发出。
 * `off` 不传回调时移除该事件的全部回调。
 */
export class MessageBus {
  /** 事件名 → 回调集合（同一事件可注册多个回调） */
  private listeners = new Map<string, Set<MessageCallback>>()

  /** 为某事件注册回调；重复注册同一回调不生效（Set 去重） */
  on(event: string, callback: MessageCallback): void {
    let set = this.listeners.get(event)
    if (!set) {
      set = new Set()
      this.listeners.set(event, set)
    }
    set.add(callback)
  }

  /**
   * 注销回调：
   * - 传回调 → 只移除该回调，其它仍注册的回调不受影响；
   * - 不传回调 → 移除该事件全部回调。
   */
  off(event: string, callback?: MessageCallback): void {
    const set = this.listeners.get(event)
    if (!set) {
      return
    }
    if (callback) {
      set.delete(callback)
      if (set.size === 0) {
        this.listeners.delete(event)
      }
    } else {
      this.listeners.delete(event)
    }
  }

  /** 发出事件：所有已注册回调均被调用，并收到携带的数据 */
  emit(event: string, payload?: unknown): void {
    const set = this.listeners.get(event)
    if (!set) {
      return
    }
    // 快照遍历：回调中注销/注册不影响本次派发
    for (const callback of [...set]) {
      callback(payload)
    }
  }

  /** 移除全部订阅（销毁时调用） */
  clear(): void {
    this.listeners.clear()
  }
}
