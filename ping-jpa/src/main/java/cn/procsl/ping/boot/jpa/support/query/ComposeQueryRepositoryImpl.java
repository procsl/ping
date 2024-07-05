package cn.procsl.ping.boot.jpa.support.query;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ComposeQueryRepositoryImpl implements ComposeQueryRepository {

    final EntityManager em;


    private boolean isWhere(Field item) {
        Where where = AnnotationUtils.findAnnotation(item, Where.class);
        return where == null || !where.only();
    }

    @Override
    public <T> List<T> queryProjections(T query) {


        Projection projection = AnnotationUtils.findAnnotation(query.getClass(), Projection.class);
        if (projection == null) {
            throw new IllegalArgumentException("Query class must have @QueryEntity annotation");
        }


        return List.of();
    }

    @Override
    @SneakyThrows
    public <T> List<T> query(T query) {

        Projection projection = AnnotationUtils.findAnnotation(query.getClass(), Projection.class);
        if (projection == null) {
            throw new IllegalArgumentException("Query class must have @QueryEntity annotation");
        }

        var constructor = ReflectionUtils.findConstructor(query.getClass());
        if (constructor.isEmpty()) {
            throw new IllegalArgumentException("Query class must have no args Constructor");
        }

        List<Field> entityFields = Arrays.asList(query.getClass().getDeclaredFields());

        // 构造创建查询字段
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> builderTupleQuery = criteriaBuilder.createTupleQuery();
        Root<?> rootObject = builderTupleQuery.from(projection.entity());
        String entityName = projection.entity().getSimpleName().toLowerCase();
        List<Object[]> noJoinFields = entityFields.stream()
            .filter(this::isWhere)
            .map(
                (k) -> {
                    // 添加非join字段
                    String alias = "%s_%s".formatted(entityName, k.getName().toLowerCase());
                    return new Object[]{k, rootObject.get(k.getName()).alias(alias)};
                }
            )
            .toList();

        // TODO
        builderTupleQuery.multiselect(noJoinFields.stream().map(item -> (Selection<?>) item[1]).collect(Collectors.toUnmodifiableList()));

        // TODO 构建join字段

        // 构建查询条件
        Map<String, List<Object[]>> where = entityFields.stream().map(item -> {
            Where annotation = AnnotationUtils.findAnnotation(item, Where.class);
            if (annotation == null) {
                return null;
            }
            return new Object[]{item, annotation};
        }).filter(Objects::nonNull).collect(Collectors.groupingBy(item -> {
            Where we = (Where) item[1];
            return we.groupName();
        }));

        List<Predicate> wheres = andWheres(query, where, rootObject, builderTupleQuery, criteriaBuilder);
        builderTupleQuery.where(wheres.toArray(new Predicate[0]));

        // 构建排序
        List<Order> orders = entityFields.stream()
            .map(item -> {
                OrderBy annotation = AnnotationUtils.findAnnotation(item, OrderBy.class);
                if (annotation == null) {
                    return null;
                }
                return new Object[]{item, annotation};
            })
            .filter(Objects::nonNull)
//            .sorted(Comparator.comparingInt(p -> ((OrderBy) p[1]).sort()))
            .map(item -> {
                OrderBy queryOrder = (OrderBy) item[1];
                Field field = (Field) item[0];
                Root<?> root = this.getRoot(field, rootObject, builderTupleQuery);
                Path<Object> path = root.get(field.getName());
                if (queryOrder.order() == OrderBy.Sort.asc) {
                    return criteriaBuilder.asc(path);
                }
                if (queryOrder.order() == OrderBy.Sort.desc) {
                    return criteriaBuilder.desc(path);
                }
                return null;
            })
            .toList();

        builderTupleQuery.orderBy(orders);

        // 解析class实例, 获取关联关系
        TypedQuery<Tuple> executor = em.createQuery(builderTupleQuery);
        List<Tuple> tmp = executor.getResultList();

        return tmp.stream().map(
            item -> {
                // 创建实例
                Object result = newInstance(constructor);
                for (Object[] selections : noJoinFields) {
                    Field field = (Field) selections[0];
                    Selection<?> selection = (Selection<?>) selections[1];
                    // 注入值
                    ReflectionUtils.setField(field, result, item.get(selection.getAlias()));
                }
                return (T) (result);
            }
        ).toList();
    }

    @SneakyThrows
    private Object newInstance(Optional<Constructor<?>> constructor) {
        return constructor.get().newInstance();
    }

    private <T> List<Predicate> andWheres(T query, Map<String, List<Object[]>> where,
                                          Root<?> rootObject,
                                          CriteriaQuery<Tuple> builderTupleQuery,
                                          CriteriaBuilder criteriaBuilder) throws IllegalAccessException {
        if (where.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<Predicate> predicates = new ArrayList<>(where.size());
        for (Map.Entry<String, List<Object[]>> entry : where.entrySet()) {
            List<Predicate> orWheres = orWhere(query, entry.getValue(), rootObject, builderTupleQuery, criteriaBuilder);
            predicates.add(criteriaBuilder.or(orWheres.toArray(new Predicate[0])));
        }
        return predicates;
    }

    private <T> List<Predicate> orWhere(T query, List<Object[]> v, Root<?> rootObject,
                                        CriteriaQuery<Tuple> builderTupleQuery,
                                        CriteriaBuilder criteriaBuilder) throws IllegalAccessException {

        if (v.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<Predicate> predicates = new ArrayList<>(v.size());
        for (Object[] current : v) {

            Field field = (Field) current[0];
            Where where = (Where) current[1];
            field.setAccessible(true);
            Object value = field.get(query);

            Predicate pt;
            if (where.isForce()) {
                pt = forcePredicate(field, rootObject, builderTupleQuery, where, criteriaBuilder, value);
                predicates.add(pt);
                continue;
            }

            if (value == null) {
                continue;
            }

            if (value instanceof CharSequence v1 && v1.isEmpty()) {
                continue;
            }

            if (value instanceof Collection<?> v1 && v1.isEmpty()) {
                continue;
            }
            pt = predicate(field, rootObject, builderTupleQuery, where, criteriaBuilder, value);
            predicates.add(pt);
        }

        return predicates;
    }


    // TODO
    private Predicate forcePredicate(Field field, Root<?> rootObject, CriteriaQuery<Tuple> builderTupleQuery,
                                     Where where, CriteriaBuilder criteriaBuilder, Object value) {

        return predicate(field, rootObject, builderTupleQuery, where, criteriaBuilder, value);
    }

    private Predicate predicate(Field field, Root<?> rootObject, CriteriaQuery<Tuple> builderTupleQuery,
                                Where where, CriteriaBuilder criteriaBuilder, Object value) {
        Root<?> root = getRoot(field, rootObject, builderTupleQuery);

        Path<Object> path = root.get(field.getName());
        if (where.predicate() == Where.WherePredicate.equal) {
            return criteriaBuilder.equal(path, value);
        }
        if (where.predicate() == Where.WherePredicate.like) {

            if (value instanceof String v1) {
                return criteriaBuilder.like(root.get(field.getName()), v1);
            }
            throw new IllegalArgumentException("TODO");
        }
        // TODO
        return null;
    }

    private Root<?> getRoot(Field field, Root<?> rootObject, CriteriaQuery<Tuple> builderTupleQuery) {
        JoinOn joined = AnnotationUtils.findAnnotation(field, JoinOn.class);
        Root<?> root;
        if (joined == null) {
            root = rootObject;
        } else {
            // TODO 需要缓存
            root = builderTupleQuery.from(joined.joinEntity());
        }
        return root;
    }

}
