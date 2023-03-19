//package cn.procsl.ping.boot.common.utils;
//
//import com.querydsl.core.BooleanBuilder;
//import com.querydsl.core.types.*;
//import com.querydsl.core.types.dsl.*;
//import com.querydsl.jpa.JPQLQuery;
//import lombok.extern.slf4j.Slf4j;
//import lombok.val;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//
//import java.util.Iterator;
//import java.util.function.Supplier;
//
//@Slf4j
//public final class QueryBuilder<T> {
//
//    final JPQLQuery<T> query;
//    final BooleanBuilder builder;
//
//    private QueryBuilder(JPQLQuery<T> query) {
//        this.query = query;
//        this.builder = new BooleanBuilder();
//    }
//
//    public static <T> QueryBuilder<T> builder(JPQLQuery<T> query) {
//        return new QueryBuilder<>(query);
//    }
//
//    public QueryBuilder<T> and(boolean condition, Supplier<Predicate> supplier) {
//        if (condition) {
//            builder.and(supplier.get());
//        }
//        return this;
//    }
//
//    public QueryBuilder<T> and(String condition, Supplier<Predicate> supplier) {
//        if (condition == null || condition.isEmpty()) {
//            return this;
//        }
//        builder.and(supplier.get());
//        return this;
//    }
//
//    public QueryBuilder<T> or(boolean condition, Supplier<Predicate> supplier) {
//        if (condition) {
//            builder.or(supplier.get());
//        }
//        return this;
//    }
//
//    public JPQLQuery<T> build(Pageable pageable) {
//        if (pageable.isPaged()) {
//            query.limit(pageable.getPageSize());
//            query.offset(pageable.getOffset());
//        }
//
//        if (!pageable.getSort().isSorted()) {
//            return this.build();
//        }
//
//        Expression<?> projection = this.query.getMetadata().getProjection();
//        if (projection == null) {
//            return this.build();
//        }
//
//        Sort sorts = pageable.getSort();
//        Iterator<Sort.Order> iterator = sorts.iterator();
//        SelectVisitor visitor = new SelectVisitor();
//        while (iterator.hasNext()) {
//            Sort.Order item = iterator.next();
//            Expression<? extends Comparable<?>> express = projection.accept(visitor, item);
//            if (express == null) {
//                continue;
//            }
//            Order order = item.getDirection().isAscending() ? Order.ASC : Order.DESC;
//            OrderSpecifier.NullHandling handling;
//            switch (item.getNullHandling()) {
//                case NULLS_LAST:
//                    handling = OrderSpecifier.NullHandling.NullsLast;
//                    break;
//                case NULLS_FIRST:
//                    handling = OrderSpecifier.NullHandling.NullsFirst;
//                    break;
//                case NATIVE:
//                default:
//                    handling = OrderSpecifier.NullHandling.Default;
//            }
//            OrderSpecifier<?> specifier = new OrderSpecifier<>(order, express, handling);
//            this.query.orderBy(specifier);
//        }
//        return this.build();
//    }
//
//    public JPQLQuery<T> build() {
//        query.where(this.builder);
//        return this.query;
//    }
//
//    static class SelectVisitor implements Visitor<Expression<? extends Comparable<?>>, Sort.Order> {
//        @Override
//        public Expression<? extends Comparable<?>> visit(Constant<?> expr, Sort.Order context) {
//            return null;
//        }
//
//        @Override
//        public Expression<? extends Comparable<?>> visit(FactoryExpression<?> expr, Sort.Order context) {
//            for (Expression<?> arg : expr.getArgs()) {
//                Expression<? extends Comparable<?>> result = arg.accept(this, context);
//                if (result == null) {
//                    continue;
//                }
//                return result;
//            }
//            return null;
//        }
//
//        @Override
//        public Expression<? extends Comparable<?>> visit(Operation<?> expr, Sort.Order context) {
//            return null;
//        }
//
//        @Override
//        public Expression<? extends Comparable<?>> visit(ParamExpression<?> expr, Sort.Order context) {
//            return null;
//        }
//
//        @Override
//        public Expression<? extends Comparable<?>> visit(Path<?> expr, Sort.Order context) {
//            if (context == null) {
//                return null;
//            }
//
//            if (context.getProperty().contains(".")) {
//                String[] array = context.getProperty().split("\\.");
//                if (array.length != 2) {
//                    return null;
//                }
//                boolean bool = array[0].equals(expr.getRoot().getMetadata().getName())
//                        && array[1].equals(expr.getMetadata().getName());
//                if (!bool) {
//                    return null;
//                }
//            } else {
//                String name = expr.getMetadata().getName();
//                if (!name.equals(context.getProperty())) {
//                    return null;
//                }
//            }
//
//            boolean bool = (expr instanceof PathImpl && Comparable.class.isAssignableFrom(expr.getType()))
//                    || expr instanceof NumberPath
//                    || expr instanceof StringPath
//                    || expr instanceof EnumPath
//                    || expr instanceof ComparablePath
//                    || expr instanceof BooleanPath
//                    || expr instanceof DatePath
//                    || expr instanceof DateTimePath
//                    || expr instanceof TimePath;
//
//            if (bool) {
//                @SuppressWarnings("unchecked")
//                val con = (Expression<? extends Comparable<?>>) expr;
//                log.debug("add sort property:[{}]", context.getProperty());
//                return con;
//            }
//            return null;
//
//        }
//
//        @Override
//        public Expression<? extends Comparable<?>> visit(SubQueryExpression<?> expr, Sort.Order context) {
//            return null;
//        }
//
//        @Override
//        public Expression<? extends Comparable<?>> visit(TemplateExpression<?> expr, Sort.Order context) {
//            return null;
//        }
//    }
//
//}
