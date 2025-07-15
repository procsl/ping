package cn.procsl.ping.boot.jpa.support.query.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class SimpleQueryObjectBuilderTest {

    @Test
    public void toBaseClauseString() {
        SimpleQueryObjectBuilder builder = new SimpleQueryObjectBuilder();
        BuilderContext context = new BuilderContext() {
            @Override
            public boolean isFormat() {
                return true;
            }
        };

        builder.addFrom("TableA", "a");
        builder.addFrom("TableB", "b");
        builder.addFrom("TableC", "c");
        builder.addSelect("a.id", "aid");
        builder.addSelect("b.id", "bid");
        builder.addSelect("c.id", "bid");

        String string = builder.toClauseString(context);
        log.info("sql语句: {}", string);
    }

    @Test
    public void toWhereClauseString() {
        SimpleQueryObjectBuilder builder = new SimpleQueryObjectBuilder();
        BuilderContext context = new BuilderContext() {
            @Override
            public boolean isFormat() {
                return true;
            }
        };

        builder.addFrom("TableA", "a");
        builder.addFrom("TableB", "b");
        builder.addSelect("a.id", "aid");
        builder.addSelect("b.id", "bid");
        Operator and = LogicalOperator.and(Operator.eq("a.id", "b.id"), Operator.like("b.name", ":name"));
        Operator or = LogicalOperator.or(Operator.eq("c.nickNam", ":nickName"), Operator.notIn("c.nickName", ":notNicks"));
        builder.setWhere(new SimpleWhere(LogicalOperator.and(and, Operator.group(or))));
        String string = builder.toClauseString(context);
        log.info("sql语句: {}", string);
    }

    @Test
    public void toJoinClauseString() {
        SimpleQueryObjectBuilder builder = new SimpleQueryObjectBuilder();
        BuilderContext context = new BuilderContext() {
            @Override
            public boolean isFormat() {
                return true;
            }
        };

        builder.addSelect("a.id", "aid");
        builder.addSelect("b.id", "bid");

        SimpleFrom sfa = new SimpleFrom("TableA", "a");

        SimpleFrom sfb = new SimpleFrom("TableB", "b");
        sfa.addJoinObject(JoinObject.JOIN_TYPE.inner, sfb, Operator.eq("a.id", "b.id"));

        SimpleFrom sfc = new SimpleFrom("TableC", "c");
        sfb.addJoinObject(JoinObject.JOIN_TYPE.left, sfc, Operator.eq("b.id", "c.id"));

        SimpleFrom sfd = new SimpleFrom("TableD", "d");
        sfc.addJoinObject(JoinObject.JOIN_TYPE.left, sfd, Operator.eq("c.id", "d.id"));

        SimpleFrom sfe = new SimpleFrom("TableE", "e");
        sfa.addJoinObject(JoinObject.JOIN_TYPE.left, sfe, Operator.like("a.id", "e.id"));

        builder.addFrom(sfa);
        String string = builder.toClauseString(context);
        log.info("sql语句: [{}]", string);
    }

    @Test
    public void toFullClauseString() {
        SimpleQueryObjectBuilder builder = new SimpleQueryObjectBuilder();
        BuilderContext context = new BuilderContext() {
            @Override
            public boolean isFormat() {
                return true;
            }
        };

        builder.addSelect("a.id", "aid");
        builder.addSelect("b.id", "bid");

        SimpleFrom sfa = new SimpleFrom("TableA", "a");

        SimpleFrom sfb = new SimpleFrom("TableB", "b");
        sfa.addJoinObject(JoinObject.JOIN_TYPE.inner, sfb, Operator.eq("a.id", "b.id"));

        SimpleFrom sfc = new SimpleFrom("TableC", "c");
        sfb.addJoinObject(JoinObject.JOIN_TYPE.left, sfc, "b.id", "c.id");

        SimpleFrom sfd = new SimpleFrom("TableD", "d");
        sfc.addJoinObject(JoinObject.JOIN_TYPE.left, sfd, Operator.eq("c.id", "d.id"));

        SimpleFrom sfe = new SimpleFrom("TableE", "e");
        sfa.addJoinObject(JoinObject.JOIN_TYPE.left, sfe, Operator.like("a.id", "e.id"));

        SimpleFrom sff = new SimpleFrom("TableF", "f");
        sfc.addJoinObject(JoinObject.JOIN_TYPE.inner, sff, Operator.ne("c.id", "f.id"));

        builder.addFrom(sfa);

        builder.setWhere(new SimpleWhere(LogicalOperator.and(Operator.in("a.id", ":ids"), Operator.like("b.name", ":name"))));
        builder.addSort(SortObject.forDesc("a.id"), SortObject.forAsc("b.id"), SortObject.forDesc("c.id"));

        String string = builder.toClauseString(context);
        log.info("sql语句: [{}]", string);
    }

}
