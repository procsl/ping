package cn.procsl.ping.boot.ui.compiler;

import cn.procsl.ping.boot.ui.domain.Component;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Transformer 扩展点契约测试（任务 1.4.1）：纯变换 List → List，支持过滤/增删/改字段。
 */
class TransformerContractTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private final CompileContext emptyContext = new CompileContext(List.of());

    private Component node(String type, String id) {
        return new Component(type, id, null, List.of());
    }

    @Test
    void filterImplementationRemovesNodes() {
        Transformer dropPage = (nodes, ctx) -> nodes.stream()
            .filter(n -> !n.isType("page"))
            .toList();

        List<Component> input = List.of(node("page", "user"), node("function", "list"));
        List<Component> output = dropPage.process(input, emptyContext);

        assertEquals(1, output.size());
        assertEquals("list", output.get(0).getId());
    }

    @Test
    void transformImplementationRebuildsNodes() {
        ObjectNode props = mapper.createObjectNode();
        props.put("operationId", "findUsers");
        Component function = new Component("function", "list", props, List.of());

        Transformer addTag = (nodes, ctx) -> nodes.stream()
            .map(n -> {
                if (!n.isType("function")) {
                    return n;
                }
                ObjectNode rebuilt = ((ObjectNode) n.getProperties().deepCopy()).put("tagged", true);
                return new Component(n.getType(), n.getId(), rebuilt, n.getChildren());
            })
            .toList();

        List<Component> output = addTag.process(List.of(function), emptyContext);

        assertEquals(1, output.size());
        assertNotSame(function, output.get(0)); // Component 不可变：改字段 = 重建节点
        assertTrue(output.get(0).getProperties().get("tagged").asBoolean());
    }

    @Test
    void validatorReturnsInputWhenPassing() {
        List<Component> input = List.of(node("page", "user"));

        Transformer validator = (nodes, ctx) -> nodes; // 通过：原样返回
        List<Component> output = validator.process(input, emptyContext);

        assertSame(input, output);
    }

    @Test
    void validatorThrowsCompileExceptionOnFailure() {
        Transformer dupChecker = (nodes, ctx) -> {
            if (nodes.stream().map(Component::getId).distinct().count() != nodes.size()) {
                throw new cn.procsl.ping.boot.ui.exception.ComposeException("Duplicate id detected");
            }
            return nodes;
        };

        List<Component> input = List.of(node("page", "user"), node("page", "user"));
        assertThrows(cn.procsl.ping.boot.ui.exception.ComposeException.class,
            () -> dupChecker.process(input, emptyContext));
    }

    @Test
    void multipleStagesChainTogether() {
        Transformer first = (nodes, ctx) -> {
            List<Component> result = new ArrayList<>(nodes);
            result.add(node("page", "injected"));
            return result;
        };
        Transformer second = (nodes, ctx) -> nodes.stream()
            .filter(n -> !n.getId().equals("user"))
            .toList();

        // 模拟 ComposeCompiler 的链式语义：nodes = t.process(nodes, ctx)
        List<Component> nodes = List.of(node("page", "user"));
        nodes = first.process(nodes, new CompileContext(List.of()));
        nodes = second.process(nodes, new CompileContext(List.of()));

        assertEquals(1, nodes.size());
        assertEquals("injected", nodes.get(0).getId());
    }
}
