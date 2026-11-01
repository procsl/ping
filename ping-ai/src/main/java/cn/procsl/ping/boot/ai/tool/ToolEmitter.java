package cn.procsl.ping.boot.ai.tool;

/**
 * 工具对象投递端口：工具开始与结束时各发一条可渲染对象。
 */
public interface ToolEmitter {

    /**
     * 工具开始。
     *
     * @param name      工具名
     * @param arguments 原始参数（JSON 字符串）
     * @return 对象 id
     */
    int begin(String name, String arguments);

    /**
     * 工具结束。
     *
     * @param id      对象 id
     * @param status  状态：ok / denied / failed
     * @param summary 可渲染摘要
     */
    void end(int id, String status, String summary);
}
