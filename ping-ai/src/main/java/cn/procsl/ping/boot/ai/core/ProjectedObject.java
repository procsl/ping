package cn.procsl.ping.boot.ai.core;

/**
 * 由消息表投影出的对象。
 *
 * @param id      对象 id（按消息顺序从 0 递增）
 * @param type    对象类型（实现约定）
 * @param content 完整内容
 * @param handled 是否已处理（仅错误对象）
 */
public record ProjectedObject(int id, String type, String content, boolean handled) {
}
