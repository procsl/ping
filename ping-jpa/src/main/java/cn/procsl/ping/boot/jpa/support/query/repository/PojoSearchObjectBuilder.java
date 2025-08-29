package cn.procsl.ping.boot.jpa.support.query.repository;

import cn.procsl.ping.boot.jpa.support.query.From;
import cn.procsl.ping.boot.jpa.support.query.Join;
import cn.procsl.ping.boot.jpa.support.query.Projection;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class PojoSearchObjectBuilder implements SearchObject {

    private final SimpleSearchObjectBuilder builder = new SimpleSearchObjectBuilder();

    private final Object targetQueryPojo;

    private Class<? extends Serializable> returnType;

    private boolean init = false;

    public void init() {
        if (init) {
            return;
        }

        // 解析查询参数
        Class<?> clazz = this.targetQueryPojo.getClass();
        Projection projection = AnnotationUtils.findAnnotation(clazz, Projection.class);
        if (projection == null) {
            throw new IllegalArgumentException("参数 pojo 未标注 @Projection 注解");
        }
        this.returnType = projection.returnType();
        for (Projection.Item item : projection.value()) {
            builder.addSelect(item.value(), item.alias());
        }

        // 解析主表,暂时不支持多个From
        From main = AnnotatedElementUtils.findMergedAnnotation(clazz, From.class);
        if (main == null) {
            throw new IllegalArgumentException("参数 pojo 未标注 @From 注解");
        }

        // 解析所有的join
        Set<Join> joins = AnnotatedElementUtils.findMergedRepeatableAnnotations(clazz, Join.class);
        SimpleFrom mainBuilder = new SimpleFrom(main.entity().getName(), main.alias());
        resolverJoinFrom(joins, main, mainBuilder);
        // 只将main加入
        this.builder.addFrom(mainBuilder);

        // 解析Where语句
        var predicateProps = PredicateAnnotationExtractor.extract(this.targetQueryPojo);
        // 按照group分组
        Map<String, List<PredicateMeta>> groups = predicateProps.stream().collect(Collectors.groupingBy(item -> item.getPredicate().group()));
        ArrayList<Operator> ops = new ArrayList<>();
        HashSet<Parameter> p = new HashSet<>();
        groups.forEach((k, v) -> {
            ArrayList<Operator> group = new ArrayList<>();
            for (PredicateMeta prop : predicateProps) {
                if (prop.shouldIgnore()) {
                    continue;
                }
                Operator op = prop.createOperator(main);
                p.addAll(prop.extractParameterPlaceholderAndValue());
                group.add(op);
            }
            ops.add(Operator.group(LogicalOperator.or(group)));
        });

        this.builder.setWhere(new SimpleWhere(LogicalOperator.and(ops), p.toArray(value -> new Parameter[0])));
        this.init = true;
    }

    private static void resolverJoinFrom(Set<Join> joins, From main, SimpleFrom mainBuilder) {
        if (joins.isEmpty()) {
            return;
        }

        HashMap<String, Set<Join>> refs = new HashMap<>();
        HashMap<String, From> froms = new HashMap<>();
        HashMap<String, SimpleFrom> fromBuilder = new HashMap<>();
        fromBuilder.put(main.alias(), mainBuilder);
        froms.put(main.alias(), main);

        // 解析join字段
        for (Join join : joins) {
            String alias = join.join().alias();
            froms.put(alias, join.join());
            // 防止重复创建导致引用关系错误
            if (!fromBuilder.containsKey(alias)) {
                fromBuilder.put(alias, new SimpleFrom(join.join().entity().getName(), alias));
            }
            addJoin(join, refs);
        }
        // 创建所有join关系
        refs.forEach((k, v) -> {
            // 这是主表
            SimpleFrom master = fromBuilder.get(k);
            // 从表, 多个被join
            for (Join join : v) {
                // 从表对象
                SimpleFrom slv = fromBuilder.get(join.join().alias());
                Operator eq = Operator.eq("%s.%s".formatted(master.alias(), join.leftJoinField()), "%s.%s".formatted(slv.alias(), join.rightJoinField()));
                master.addJoinObject(join.type(), slv, eq);
            }

        });
    }

    private static void addJoin(Join join, HashMap<String, Set<Join>> refs) {
        Set<Join> coll = refs.get(join.ref());
        if (coll == null) {
            refs.put(join.ref(), new HashSet<>());
        }
        coll = refs.get(join.ref());
        coll.add(join);
    }

    @Override
    public List<SelectObject> select() {
        init();
        return this.builder.select();
    }

    @Override
    public List<FromObject> from() {
        init();
        return this.builder.from();
    }

    @Override
    public WhereObject where() {
        init();
        return this.builder.where();
    }

    @Override
    public List<SortObject> sort() {
        init();
        return this.builder.sort();
    }

}
