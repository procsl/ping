package cn.procsl.ping.boot.jpa.support.query.ast;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class SimpleQueryStringBuilderTest {


    @Test
    @DisplayName("Select语句顺序生成测试")
    public void toClauseStringOrder() {

        SimpleQueryStringBuilder builder = new SimpleQueryStringBuilder();

        SimpleSelectClause simple = new SimpleSelectClause(new FragmentClause("tb.my_name"), "name", String.class, 1);
        log.info(simple.toString());
        Assertions.assertEquals("tb.my_name as name", simple.toClauseString());

        SimpleSelectClause simple2 = new SimpleSelectClause(new FragmentClause("tb.id"), "id", String.class, 2);
        Assertions.assertEquals("tb.id as id", simple2.toClauseString());

        builder.addSelectClause(simple2);
        builder.addSelectClause(simple);

        String res = builder.toClauseString();
        Assertions.assertEquals("select tb.my_name as name,tb.id as id from", res);
        log.info(res);
    }

    @Test
    @DisplayName("Select语句生成测试")
    public void toClauseString() {

        SimpleQueryStringBuilder builder = new SimpleQueryStringBuilder();

        SimpleSelectClause simple = new SimpleSelectClause(new FragmentClause("tb.my_name"), "name", String.class, 0);
        log.info(simple.toString());
        Assertions.assertEquals("tb.my_name as name", simple.toClauseString());

        SimpleSelectClause simple2 = new SimpleSelectClause(new FragmentClause("tb.id"), "id", String.class, 0);
        Assertions.assertEquals("tb.id as id", simple2.toClauseString());

        builder.addSelectClause(simple);
        builder.addSelectClause(simple2);

        String res = builder.toClauseString();
        Assertions.assertEquals("select tb.my_name as name,tb.id as id from", res);
        log.info(res);
    }
}
