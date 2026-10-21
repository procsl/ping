package cn.procsl.ping.boot.ui.domain;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 统一节点组合类型读写测试（任务 1.1.1）。
 */
public class ComponentTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void functionNodeCarriesAllFields() {
        ObjectNode properties = mapper.createObjectNode();
        properties.put("operationId", "findUsers");

        Component function = new Component("function", "list", properties, List.of());

        assertEquals("function", function.getType());
        assertEquals("list", function.getId());
        assertEquals("findUsers", function.getProperties().get("operationId").asString());
        assertTrue(function.getChildren().isEmpty());
    }

    @Test
    public void pageNodeCarriesChildren() {
        Component function = new Component("function", "list", null, null);
        Component page = new Component("page", "user", null, List.of(function));

        assertEquals("page", page.getType());
        assertEquals("user", page.getId());
        assertNull(page.getProperties());
        assertEquals(1, page.getChildren().size());
        assertEquals("list", page.getChildren().get(0).getId());
    }

    @Test
    public void appNodeAggregatesMultiplePages() {
        Component userPage = new Component("page", "user", null, null);
        Component dashboardPage = new Component("page", "dashboard", null, null);

        Component app = new Component("app", "ping.app", null, List.of(userPage, dashboardPage));

        assertEquals("app", app.getType());
        assertEquals(2, app.getChildren().size());
        assertEquals(List.of("user", "dashboard"),
            app.getChildren().stream().map(Component::getId).toList());
    }

    @Test
    public void nullChildrenDegradedToEmptyList() {
        Component function = new Component("function", "edit", null, null);
        assertTrue(function.getChildren().isEmpty());
    }

    @Test
    public void childrenDefensivelyCopiedAndImmutable() {
        List<Component> mutable = new ArrayList<>();
        mutable.add(new Component("function", "list", null, null));

        Component page = new Component("page", "user", null, mutable);
        mutable.clear();

        // 构造时已拷贝，外部修改不影响节点
        assertEquals(1, page.getChildren().size());
        // 节点自身不可变：改字段 = 重建节点
        assertThrows(UnsupportedOperationException.class,
            () -> page.getChildren().add(new Component("function", "edit", null, null)));
    }

    @Test
    public void dispatchByTypeDistinguishesThreeKinds() {
        Component app = new Component("app", "ping.app", null, null);
        Component page = new Component("page", "user", null, null);
        Component function = new Component("function", "list", null, null);

        assertTrue(app.isType("app"));
        assertTrue(page.isType("page"));
        assertTrue(function.isType("function"));

        assertTrue(!app.isType("page"));
        assertTrue(!page.isType("function"));
        assertTrue(!function.isType("app"));
    }

    @Test
    public void serializableThroughJavaObjectStream() throws Exception {
        ObjectNode properties = mapper.createObjectNode();
        properties.put("method", "GET");
        properties.put("path", "/v1/system/users");
        Component app = new Component("app", "ping.app", null,
            List.of(new Component("page", "user", null,
                List.of(new Component("function", "list", properties, null)))));

        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(app);
            bytes = bos.toByteArray();
        }

        Component restored;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            restored = (Component) ois.readObject();
        }

        // Component 无 equals：比较 JSON 序列化形态
        assertEquals(mapper.writeValueAsString(app), mapper.writeValueAsString(restored));
    }
}
