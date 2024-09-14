package cn.procsl.ping.boot.jpa.support.query.ast;

import cn.procsl.ping.boot.jpa.support.query.JoinField;
import cn.procsl.ping.boot.jpa.support.query.Projection;
import cn.procsl.ping.boot.jpa.support.query.ReferenceBy;
import cn.procsl.ping.boot.jpa.support.query.SelectField;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

record QueryAnnotationParser(FieldDescription root) {

    QueryAnnotationParser(FieldDescription root) {
        this.root = new CacheFieldDescription(root);
    }

    public Collection<SelectClause> getSelectClauses() {
        List<FieldDescription> children = this.root.getChildren();
        List<SelectClause> selectClauseList = new ArrayList<>(children.size());
        for (int i = 0; i < children.size(); i++) {
            SelectClause clause = createFieldClause(children.get(i), i);
            if (clause == null) {
                continue;
            }
            selectClauseList.add(clause);
        }
        return selectClauseList;
    }

    public Collection<FromClause> getFromClauses() {
        List<FieldDescription> children = this.root.getChildren();

        Projection main = this.root.findMargeAnnotation(Projection.class);
        if (main == null) {
            throw new IllegalArgumentException("No projection annotation found: " + this.root.getType());
        }

        List<JoinField> joinFields = this.root.findAnnotations(JoinField.class);
        if (!joinFields.isEmpty()) {
            ComposeJoinClause composeJoinClause = new ComposeJoinClause(new ProjectionFromClause(main));
            joinFields.forEach(joinField -> {
                joinField
                composeJoinClause.addJoin(new SimpleJoinClause());
            });
        }

        return null;
    }

    public static SelectClause createFieldClause(FieldDescription child, int i) {
        Class<?> type = child.getType();

        SelectField select = child.findMargeAnnotation(SelectField.class);
        if (select == null) {
            return null;
        }

        ReferenceBy ref = child.findMargeAnnotationOrDefault(ReferenceBy.class, () -> new InnerRef(child));
        if (ClassUtils.isSimpleType(type)) {
            return new SimpleSelectClause(new RefClause(ref), child.getFieldName(), child.getType(), i);
        }
        if (ClassUtils.isContainerType(type)) {
            return new EmptyClause();
        }
        return new ComposeSelectClause(child, i);
    }


    @RequiredArgsConstructor
    final static class InnerRef implements ReferenceBy {

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
    final static class RefClause implements Clause {

        final ReferenceBy ref;

        @Override
        public String toClauseString() {
            return "";
        }

    }

    final static class EmptyClause implements SelectClause {
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
    final static class ComposeSelectClause implements SelectClause {

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

    @RequiredArgsConstructor
    final static class CacheFieldDescription implements FieldDescription {

        final FieldDescription field;
        List<FieldDescription> ch = null;
        List<Annotation> ann = null;

        @Override
        public Optional<FieldDescription> getParentField() {
            return Optional.empty();
        }

        @Override
        public String getFieldName() {
            return field.getFieldName();
        }

        @Override
        public Class<?> getType() {
            return field.getType();
        }

        @Override
        public List<FieldDescription> getChildren() {
            if (ch == null) {
                ch = this.field.getChildren();
            }
            return ch;
        }

        @Override
        public List<Annotation> getAnnotations() {
            if (ann == null) {
                this.ann = this.field.getAnnotations();
            }
            return this.ann;
        }
    }

}
