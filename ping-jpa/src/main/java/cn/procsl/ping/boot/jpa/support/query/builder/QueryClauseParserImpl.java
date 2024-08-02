package cn.procsl.ping.boot.jpa.support.query.builder;

import cn.procsl.ping.boot.jpa.support.query.builder.def.*;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@RequiredArgsConstructor
final public class QueryClauseParserImpl implements QueryClauseParser {

    final PojoDef pojoDef;

    final FieldDefVisitor<SimpleDef, Object> simple = FieldDefVisitorImpl.ofVisite((s, o) -> s);
    final FieldDefVisitor<PojoDef, Object> pojo = FieldDefVisitorImpl.ofVisitePojo((s, o) -> s);

    @Override
    public List<SelectClause> parseSelects() {

        List<FieldDef> fields = pojoDef.getFields();
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("无可解析的查询参数");
        }

        final List<SelectClause> selectClauses = new ArrayList<>();
        extracted(fields, selectClauses);

        return selectClauses;
    }

    void extracted(List<FieldDef> fields, List<SelectClause> selectClauses) {
        for (FieldDef field : fields) {
            SimpleDef ff = field.accept(simple, null);
            if (ff != null) {
                int index = selectClauses.size();
                selectClauses.add(new SelectClauseImpl(ff, index));
                continue;
            }
            PojoDef pp = field.accept(pojo, null);
            if (pp == null) {
                continue;
            }
            List<FieldDef> newFields = pp.getFields();
            if (newFields == null || newFields.isEmpty()) {
                continue;
            }
            this.extracted(newFields, selectClauses);
        }
    }

    @Override
    public List<Clause> parseWhere() {
        return List.of();
    }

    @Override
    public List<Clause> parseOrderBy() {
        return List.of();
    }

    @Override
    public List<Clause> parseGroupBy() {
        return List.of();
    }

    @Override
    public List<FromClause> parseFrom() {
        ArrayList<FromClause> list = new ArrayList<>();
        FromClauseImpl root = new FromClauseImpl(null, this.pojoDef, 0);
        list.add(root);
        extractedPojo(this.pojoDef.getFields(), pojoDef, list);
        return list;
    }

    private void extractedPojo(List<FieldDef> def, PojoDef parent, List<FromClause> clauses) {
        for (FieldDef field : def) {
            PojoDef pp = field.accept(pojo, null);
            if (pp == null) {
                continue;
            }
            clauses.add(new FromClauseImpl(parent, pp, clauses.size()));
            List<FieldDef> newFields = pp.getFields();
            if (newFields == null || newFields.isEmpty()) {
                continue;
            }
            this.extractedPojo(newFields, pp, clauses);
        }
    }

    private static class FieldDefVisitorImpl<OUT, IN> implements FieldDefVisitor<OUT, IN> {

        private BiFunction<SimpleDef, IN, OUT> visitSimpleField;
        private BiFunction<CollectionDef, IN, OUT> visitCollectionField;
        private BiFunction<PojoDef, IN, OUT> visitPojoField;
        private BiFunction<MapDef, IN, OUT> visitMapField;
        private BiFunction<EntityRef, IN, OUT> visitEntityField;

        @Override
        public OUT visitSimpleField(SimpleDef def, IN args) {
            if (visitSimpleField == null) {
                return null;
            }
            return this.visitSimpleField.apply(def, args);
        }

        @Override
        public OUT visitCollectionField(CollectionDef def, IN args) {
            if (visitCollectionField == null) {
                return null;
            }
            return this.visitCollectionField.apply(def, args);
        }

        @Override
        public OUT visitPojoField(PojoDef def, IN args) {
            if (visitPojoField == null) {
                return null;
            }
            return this.visitPojoField.apply(def, args);
        }

        @Override
        public OUT visitMapField(MapDef def, IN args) {
            if (visitMapField == null) {
                return null;
            }
            return this.visitMapField.apply(def, args);
        }

        @Override
        public OUT visitEntityField(EntityRef def, IN args) {
            if (visitEntityField == null) {
                return null;
            }
            return this.visitEntityField.apply(def, args);
        }

        public static <OUT, IN> FieldDefVisitor<OUT, IN> ofVisite(final BiFunction<SimpleDef, IN, OUT> simple) {
            FieldDefVisitorImpl<OUT, IN> visitor = new FieldDefVisitorImpl<>();
            visitor.visitSimpleField = simple;
            return visitor;
        }

        public static <OUT, IN> FieldDefVisitor<OUT, IN> ofVisitePojo(final BiFunction<PojoDef, IN, OUT> simple) {
            FieldDefVisitorImpl<OUT, IN> visitor = new FieldDefVisitorImpl<>();
            visitor.visitPojoField = simple;
            return visitor;
        }

    }

    @RequiredArgsConstructor
    private static class SelectClauseImpl implements SelectClause {

        final SimpleDef def;
        final int index;

        @Override
        public String toClauseString() {
            EntityRef ref = this.def.getEntityRef();
            return " %s.%s as %s ".formatted(ref.getEntityAlias(), ref.getEntityFieldName(), this.getAliasName());
        }

        @Override
        public String getAliasName() {
            return "%s_%s".formatted(def.getFieldName(), index);
        }

        @Override
        public int order() {
            return index;
        }
    }

    @RequiredArgsConstructor
    private static class FromClauseImpl implements FromClause {
        private final PojoDef parent;
        private final PojoDef current;
        private final int index;

        @Override
        public String toString() {
            return this.toClauseString();
        }

        @Override
        public int hashCode() {
            return this.toClauseString().hashCode();
        }

        @Override
        public String toClauseString() {
            return "%s as %s".formatted(current.getEntityClass().getName(), this.getAliasName());
        }

        @Override
        public int order() {
            return index;
        }

        @Override
        public String getAliasName() {
            return current.getEntityAlias();
        }
    }
}
