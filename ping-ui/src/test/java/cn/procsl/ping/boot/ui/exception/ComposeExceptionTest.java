package cn.procsl.ping.boot.ui.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 异常精简到 2 个后的错误场景消息测试（任务 1.2.1）。
 *
 * <p>报错信息须含具体引用路径与声明来源（design D8 / spec 编译校验）。
 */
public class ComposeExceptionTest {

    @Test
    public void duplicatePageIdErrorCarriesValueAndOrigin() {
        String message = "Duplicate page id 'user' declared in META-INF/ping/ui/compose.json"
            + " (also declared in ping-system's compose.json)";

        ComposeException e = new ComposeException(message);

        assertTrue(e.getMessage().contains("'user'"));
        assertTrue(e.getMessage().contains("compose.json"));
        assertTrue(e.getMessage().contains("Duplicate page id"));
    }

    @Test
    public void duplicateFunctionIdErrorCarriesPageContext() {
        String message = "Duplicate function id 'list' in page 'user'"
            + " (declared in META-INF/ping/ui/compose.json)";

        ComposeException e = new ComposeException(message);

        assertTrue(e.getMessage().contains("'list'"));
        assertTrue(e.getMessage().contains("page 'user'"));
    }

    @Test
    public void brokenOperationRefErrorCarriesPathAndOrigin() {
        String message = "operationId 'findUsers' not found in OpenAPI document,"
            + " referenced by META-INF/ping/ui/compose.json page 'user' function 'list'";

        ComposeException e = new ComposeException(message);

        assertTrue(e.getMessage().contains("findUsers"));
        assertTrue(e.getMessage().contains("page 'user' function 'list'"));
    }

    @Test
    public void conflictingOperationIdErrorCarriesLocations() {
        String message = "Conflicting operationId 'findUsers' in OpenAPI document:"
            + " GET /v1/system/users and GET /v1/system/accounts";

        ComposeException e = new ComposeException(message);

        assertTrue(e.getMessage().contains("Conflicting operationId 'findUsers'"));
        assertTrue(e.getMessage().contains("/v1/system/users"));
        assertTrue(e.getMessage().contains("/v1/system/accounts"));
    }

    @Test
    public void parseErrorCarriesFileLineAndCause() {
        RuntimeException cause = new RuntimeException("Unexpected token '}' at line 12");

        ComposeParseException e = new ComposeParseException(
            "META-INF/ping/ui/compose.json", 12, "invalid compose document", cause);

        assertEquals("[META-INF/ping/ui/compose.json:12] invalid compose document", e.getMessage());
        assertEquals("META-INF/ping/ui/compose.json", e.getFilePath());
        assertEquals(12, e.getLineNumber());
        assertSame(cause, e.getCause());
    }

    @Test
    public void parseErrorUsesQuestionMarkForUnknownLine() {
        ComposeParseException e = new ComposeParseException("compose.json", null, "not an array");

        assertEquals("[compose.json:?] not an array", e.getMessage());
    }

    @Test
    public void twoExceptionsFormSoleHierarchy() {
        ComposeParseException parse = new ComposeParseException("compose.json", 1, "bad json");

        // ComposeParseException 是 ComposeException 的子类：调用方只需捕获基类
        assertInstanceOf(ComposeException.class, parse);
        // 基类支持带 cause 构造（编译错误包装底层异常）
        RuntimeException cause = new RuntimeException("boom");
        assertSame(cause, new ComposeException("compile failed", cause).getCause());
    }
}
