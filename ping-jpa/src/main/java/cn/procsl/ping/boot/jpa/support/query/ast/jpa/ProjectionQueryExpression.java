package cn.procsl.ping.boot.jpa.support.query.ast.jpa;

import cn.procsl.ping.boot.jpa.support.query.*;
import cn.procsl.ping.boot.jpa.support.query.ast.*;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

final class ProjectionQueryExpression implements Expression {

    final private List<FromExpression> froms = new ArrayList<>();
    final private List<WhereExpression> wheres = new ArrayList<>();
    final private List<OrderFieldExpression> orders = new ArrayList<>();

    final private static Predicate<WhereExpression> ve = WhereExpression::isInclude;
    final private static Function<Expression, String> ee = Expression::toExpString;
    final private static Comparator<OrderFieldExpression> ss = Comparator.comparingInt(OrderFieldExpression::sort);
    final private AtomicInteger i = new AtomicInteger(0);

    final private String delimiter;
//    final private ResultExtractor<T> result;

    public <R> ProjectionQueryExpression(boolean formatter) {
        delimiter = ",\n\t";
    }

    @Override
    public String toExpString() {
        String base = "select\n\t%s\nfrom\n\t%s";
        String whereStr = this.createWhere();
        String fromStr = froms.stream().map(ee).collect(Collectors.joining(delimiter));
        String ord = orders.stream().sorted(ss).map(ee).collect(Collectors.joining(","));
        String ordersStr = this.orders.isEmpty() ? "" : "\norder by\n\t" + ord;
//        this.constructor.setTargetType(this.mapping);
//        return base.formatted(this.constructor.toExpString(), fromStr) + whereStr + ordersStr;
        return null;
    }

    public String totalExpString() {
        String base = "select\n\tcount(1)\nfrom\n\t";
        String whereStr = this.createWhere();
        String fromStr = froms.stream().map(ee).collect(Collectors.joining(delimiter));
        return base + fromStr + whereStr;
    }

    private String createWhere() {
        Collector<WhereExpression, ?, Map<String, List<WhereExpression>>> aa = Collectors.groupingBy(item -> {
            if (item.groupName() == null || item.groupName().isEmpty()) {
                return i.incrementAndGet() + "";
            }
            return item.groupName();
        });
        Map<String, List<WhereExpression>> group = wheres.stream().filter(ve).collect(aa);

        ArrayList<String> list = new ArrayList<>();
        group.forEach((k, v) -> {
            if (v.size() == 1) {
                list.add(v.getFirst().toExpString());
            } else {
                String collect = v.stream().map(ee).collect(Collectors.joining(" or "));
                list.add("(" + collect + ")");
            }
        });
        return list.isEmpty() ? "" : "\nwhere\n\t" + String.join(" and ", list);
    }

    public void addFrom(FromExpression from) {
        this.froms.add(from);
    }

    public void addWhere(WhereExpression where) {
        this.wheres.add(where);
    }

    public void addOrder(OrderFieldExpression order) {
        this.orders.add(order);
    }

    /**
     * 获取sql占位符变量
     */
    public Set<Variable> getQueryVariables() {
        return this.wheres.stream().filter(ve)
            .map(WhereExpression::getParamVariable).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return this.toExpString();
    }


    public static <Q, R> ProjectionQueryExpression create(Q query, ResultExtractor<R> result) {
        Class<?> clazz = query.getClass();
        Projection projection = AnnotationUtils.findAnnotation(clazz, Projection.class);

        if (projection == null) {
            throw new IllegalStateException("未标注@Projection注解: " + clazz);
        }

        List<Join> joins = getJoinFields(clazz);
        List<Field> fields = ClassUtils.extractFields(clazz);

        ProjectionQueryExpression pqe = new ProjectionQueryExpression(true);
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            ReferenceBy ref = AnnotationUtils.findAnnotation(field, ReferenceBy.class);

            List<Where> wheres = getWheres(field);
            for (Where where : wheres) {
                pqe.addWhere(new WhereFieldExpression(query, field, projection, ref, where));
            }

            Order order = getOrder(field);
            if (order != null) {
                pqe.addOrder(new OrderFieldExpression(field, projection, ref, order, i));
            }
        }
        pqe.addFrom(new FromFieldExpression(projection, joins, clazz));
        return pqe;
    }

    private static Order getOrder(Field field) {
        return AnnotationUtils.findAnnotation(field, Order.class);
    }

    private static List<Where> getWheres(Field field) {
        List<Where> list = new ArrayList<>();

        Where.Wheres wheres = AnnotationUtils.findAnnotation(field, Where.Wheres.class);
        if (wheres != null) {
            list.addAll(Arrays.asList(wheres.value()));
        } else {
            Where where = AnnotationUtils.findAnnotation(field, Where.class);
            list.add(where);
        }
        return list;
    }

    private static List<Join> getJoinFields(Class<?> clazz) {
        List<Join> list = new ArrayList<>();

        Join.Joins joins = AnnotationUtils.findAnnotation(clazz, Join.Joins.class);
        if (joins != null) {
            list.addAll(Arrays.asList(joins.value()));
        } else {
            Join join = AnnotationUtils.findAnnotation(clazz, Join.class);
            list.add(join);
        }
        return list;
    }


}
