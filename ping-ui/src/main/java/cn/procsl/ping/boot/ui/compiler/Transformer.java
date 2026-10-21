package cn.procsl.ping.boot.ui.compiler;

import cn.procsl.ping.boot.ui.domain.Component;

import java.util.List;

/**
 * 变换接口（开闭原则扩展点，design D6）。
 *
 * <p>新增编译阶段（如未来使用意图：字段裁剪/默认值/可排序/分页模式）只需实现本接口
 * 并注册到 {@link ComposeCompiler}，不修改已有编译器代码。
 *
 * <p>契约：输入整棵树 → 输出变换后的树，纯函数式 {@code List → List}，支持
 * 过滤 / 增删节点 / 重建节点改字段（{@link Component} 不可变，改字段 = 重建节点）。
 * 校验类阶段验证通过时原样返回入参，失败抛 {@link cn.procsl.ping.boot.ui.exception.ComposeException}。
 */
public interface Transformer {

    List<Component> process(List<Component> nodes, CompileContext context);
}
