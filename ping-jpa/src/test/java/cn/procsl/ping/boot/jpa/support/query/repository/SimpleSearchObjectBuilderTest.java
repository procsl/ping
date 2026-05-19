//package cn.procsl.ping.boot.jpa.support.query.repository;
//
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//
//@Slf4j
//public class SimpleSearchObjectBuilderTest {
//
//    @Test
//    public void toBaseClauseString() {
//        SimpleSearchObjectBuilder builder = new SimpleSearchObjectBuilder();
//        BuilderContext context = new BuilderContext() {
//            @Override
//            public boolean isFormat() {
//                return true;
//            }
//        };
//
//        builder.addFrom("TableA", "a");
//        builder.addFrom("TableB", "b");
//        builder.addFrom("TableC", "c");
//        builder.addSelect("a.id", "aid");
//        builder.addSelect("b.id", "bid");
//        builder.addSelect("c.id", "bid");
//
//        String string = builder.toClauseString(context);
//        log.info("sql语句: {}", string);
//    }
//
//    @Test
//    public void toWhereClauseString() {
//        SimpleSearchObjectBuilder builder = new SimpleSearchObjectBuilder();
//        BuilderContext context = new BuilderContext() {
//            @Override
//            public boolean isFormat() {
//                return true;
//            }
//        };
//
//        builder.addFrom("TableA", "a");
//        builder.addFrom("TableB", "b");
//        builder.addSelect("a.id", "aid");
//        builder.addSelect("b.id", "bid");
//        Operator and = LogicalOperator.and(Operator.eq("a.id", "b.id"), Operator.like("b.name", ":name"));
//        Operator or = LogicalOperator.or(Operator.eq("c.nickNam", ":nickName"), Operator.not_in("c.nickName", ":notNicks"));
//        SimpleWhere sw = new SimpleWhere();
//        sw.addOperator(and, Operator.group(or));
//        builder.setWhere(sw);
//        String string = builder.toClauseString(context);
//        log.info("sql语句: {}", string);
//    }
//
//    @Test
//    public void toJoinClauseString() {
//        SimpleSearchObjectBuilder builder = new SimpleSearchObjectBuilder();
//        BuilderContext context = new BuilderContext() {
//            @Override
//            public boolean isFormat() {
//                return true;
//            }
//        };
//
//        builder.addSelect("a.id", "aid");
//        builder.addSelect("b.id", "bid");
//
//        SimpleFrom sfa = new SimpleFrom("TableA", "a");
//
//        SimpleFrom sfb = new SimpleFrom("TableB", "b");
//        sfa.addJoinObject(JoinObject.JoinType.inner, sfb, Operator.eq("a.id", "b.id"));
//
//        SimpleFrom sfc = new SimpleFrom("TableC", "c");
//        sfb.addJoinObject(JoinObject.JoinType.left, sfc, Operator.eq("b.id", "c.id"));
//
//        SimpleFrom sfd = new SimpleFrom("TableD", "d");
//        sfc.addJoinObject(JoinObject.JoinType.left, sfd, Operator.eq("c.id", "d.id"));
//
//        SimpleFrom sfe = new SimpleFrom("TableE", "e");
//        sfa.addJoinObject(JoinObject.JoinType.left, sfe, Operator.like("a.id", "e.id"));
//
//        builder.addFrom(sfa);
//        String string = builder.toClauseString(context);
//        log.info("sql语句: [{}]", string);
//    }
//
//    @Test
//    public void toFullClauseString() {
//        SimpleSearchObjectBuilder builder = new SimpleSearchObjectBuilder();
//        BuilderContext context = new BuilderContext() {
//            @Override
//            public boolean isFormat() {
//                return true;
//            }
//        };
//
//        builder.addSelect("a.id", "aid");
//        builder.addSelect("b.id", "bid");
//
//        SimpleFrom sfa = new SimpleFrom("TableA", "a");
//
//        SimpleFrom sfb = new SimpleFrom("TableB", "b");
//        sfa.addJoinObject(JoinObject.JoinType.inner, sfb, Operator.eq("a.id", "b.id"));
//
//        SimpleFrom sfc = new SimpleFrom("TableC", "c");
//        sfb.addJoinObject(JoinObject.JoinType.left, sfc, "b.id", "c.id");
//
//        SimpleFrom sfd = new SimpleFrom("TableD", "d");
//        sfc.addJoinObject(JoinObject.JoinType.left, sfd, Operator.eq("c.id", "d.id"));
//
//        SimpleFrom sfe = new SimpleFrom("TableE", "e");
//        sfa.addJoinObject(JoinObject.JoinType.left, sfe, Operator.like("a.id", "e.id"));
//
//        SimpleFrom sff = new SimpleFrom("TableF", "f");
//        sfc.addJoinObject(JoinObject.JoinType.inner, sff, Operator.ne("c.id", "f.id"));
//
//        builder.addFrom(sfa);
//
//        SimpleWhere sw = new SimpleWhere();
//        sw.addOperator(Operator.in("a.id", ":ids"), Operator.like("b.name", ":name"));
//        builder.setWhere(sw);
//        builder.addSort(SortObject.forDesc("a.id"), SortObject.forAsc("b.id"), SortObject.forDesc("c.id"));
//
//        String string = builder.toClauseString(context);
//        log.info("sql语句: [{}]", string);
//    }
//
//}
