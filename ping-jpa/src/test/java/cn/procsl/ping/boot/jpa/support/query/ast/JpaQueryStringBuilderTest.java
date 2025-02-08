package cn.procsl.ping.boot.jpa.support.query.ast;

import jakarta.persistence.criteria.JoinType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class JpaQueryStringBuilderTest {


    @Test
    @DisplayName("Select语句顺序生成测试")
    public void toClauseStringOrder() {

        JpaQueryStringBuilder builder = new JpaQueryStringBuilder();

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

        JpaQueryStringBuilder builder = new JpaQueryStringBuilder();

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

    @Test
    @DisplayName("简单From语句生成测试")
    public void toClauseString3() {
        JpaQueryStringBuilder builder = new JpaQueryStringBuilder();
        SimpleSelectClause simple = new SimpleSelectClause(new FragmentClause("tb.my_name"), "name", String.class, 0);
        log.info(simple.toString());
        Assertions.assertEquals("tb.my_name as name", simple.toClauseString());
        builder.addSelectClause(simple);

        FromClause fromClause = new SimpleFromClause(new FragmentClause("my_table"), "tb");
        builder.addFromClause(fromClause);
        log.info("sql [{}]", builder.toClauseString());
    }

    @Test
    @DisplayName("From join语句生成测试")
    public void toClauseString4() {
        JpaQueryStringBuilder builder = new JpaQueryStringBuilder();
        {
            SimpleSelectClause simple = new SimpleSelectClause(new FragmentClause("my.my_name"), "name", String.class, 0);
            builder.addSelectClause(simple);
        }

        {
            SimpleSelectClause simple = new SimpleSelectClause(new FragmentClause("you.you_name"), "name2", String.class, 0);
            builder.addSelectClause(simple);
        }

        SimpleFromClause mainClause = new SimpleFromClause(new FragmentClause("my_table"), "my");

        SimpleFromClause youClause = new SimpleFromClause(new FragmentClause("you_table"), "you");

        SimpleFromClause heClause = new SimpleFromClause(new FragmentClause("he_table"), "he");

        SimpleFromClause sheClause = new SimpleFromClause(new FragmentClause("she_table"), "she");

        SimpleFromClause otherFrom = new SimpleFromClause(new FragmentClause("other_table"), "other");

        SimpleFromClause subFrom = new SimpleFromClause(new FragmentClause("(select sub_tab.id from sub_tab)"), "sub");

        ComposeJoinClause composeJoinClause = new ComposeJoinClause(mainClause);
//        composeJoinClause.joinTo(JoinType.INNER, youClause, composeJoinClause.getTableAlias(), "id", "id");
//        composeJoinClause.joinTo(JoinType.LEFT, heClause, composeJoinClause.getTableAlias(), "id", "id");
//        composeJoinClause.joinTo(JoinType.LEFT, sheClause, composeJoinClause.getTableAlias(), "id", "id");

        builder.addFromClause(composeJoinClause);
        builder.addFromClause(otherFrom);
        builder.addFromClause(subFrom);

        String ANSI_RESET = "\u001B[0m";
        String ANSI_RED = "\u001B[31m";
        String ANSI_GREEN = "\u001B[32m";
        String BOLD = "\u001B[1m";
        String BLUE = "\u001B[34m";
        String sql = builder.toClauseString();
        sql = sql.replaceAll("select", ANSI_RED + BOLD + "select" + ANSI_RESET);
        sql = sql.replaceAll("as", ANSI_RED + BOLD + "as" + ANSI_RESET);
        sql = sql.replaceAll("from", ANSI_RED + BOLD + "from" + ANSI_RESET);
        sql = sql.replaceAll("inner", ANSI_RED + BOLD + "inner" + ANSI_RESET);
        sql = sql.replaceAll("left", ANSI_RED + BOLD + "left" + ANSI_RESET);
        sql = sql.replaceAll("join", ANSI_RED + BOLD + "join" + ANSI_RESET);
        sql = sql.replaceAll("=", ANSI_RED + BOLD + "=" + ANSI_RESET);
        sql = sql.replaceAll("on", ANSI_RED + BOLD + "on" + ANSI_RESET);
        sql = sql.replaceAll("\\(", ANSI_GREEN + BOLD + "(" + ANSI_RESET);
        sql = sql.replaceAll("\\)", ANSI_GREEN + BOLD + ")" + BLUE);
        log.info("sql [\n{}\n]", sql);
    }

}
