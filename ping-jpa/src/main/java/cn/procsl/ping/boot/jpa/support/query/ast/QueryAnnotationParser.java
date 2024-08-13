package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.support.query.ReferenceBy;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
final class QueryAnnotationParser {

    final ProjectionFieldDescription description;


    public Collection<SelectClause> getSelectClauses() {
        List<FieldDescription> children = this.description.getChildren();
        List<SelectClause> selectClauseList = new ArrayList<>(children.size());
        for (int i = 0; i < children.size(); i++) {
            SelectClause clause = createFieldClause(children.get(i), i);
            selectClauseList.add(clause);
        }
        return selectClauseList;
    }

    public static SelectClause createFieldClause(FieldDescription child, int i) {
        Class<?> type = child.getType();
        if (ClassUtils.isSimpleType(type)) {
            ReferenceBy ref = ClassUtils.createMargeAnnotationOrDefault(ReferenceBy.class,
                child.getAnnotations(), () -> new InnerRef(child));
            return new SimpleSelectClause(new RefClause(ref), child.getFieldName(), child.getType(), i);
        }
        if (ClassUtils.isContainerType(type)) {
            return new EmptyClause();
        }
        return new ComposeSelectClause(child, i);
    }


    @RequiredArgsConstructor
    static class InnerRef implements ReferenceBy {

        final FieldDescription node;

        @Override
        public String value() {
            return "";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return ReferenceBy.class;
        }
    }

    @RequiredArgsConstructor
    static class RefClause implements Clause {

        final ReferenceBy ref;

        @Override
        public String toClauseString() {
            return "";
        }

    }

    static final class EmptyClause implements SelectClause {
        @Override
        public String getSelectFieldAlias() {
            return "";
        }

        @Override
        public Class<?> getFieldMappingType() {
            return null;
        }

        @Override
        public String toClauseString() {
            return "";
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }

    @RequiredArgsConstructor
    static class ComposeSelectClause implements SelectClause {

        final FieldDescription field;
        final int order;

        @Override
        public String getSelectFieldAlias() {
            return field.getFieldName();
        }

        @Override
        public Class<?> getFieldMappingType() {
            return field.getType();
        }

        @Override
        public String toClauseString() {
            // TODO
            return "";
        }

        @Override
        public int getOrder() {
            return order;
        }
    }

}
