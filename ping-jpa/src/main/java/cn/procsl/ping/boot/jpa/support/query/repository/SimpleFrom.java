package cn.procsl.ping.boot.jpa.support.query.repository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public final class SimpleFrom implements FromObject {
    private final String baseFormClause;
    private final String aliasName;
    private final List<JoinObject> joins = new ArrayList<>();

    public SimpleFrom(String baseFormClause, String aliasName, JoinObject... joinObjects) {
        this.baseFormClause = baseFormClause;
        this.aliasName = aliasName;
        if (joinObjects != null) {
            this.joins.addAll(Arrays.asList(joinObjects));
        }
    }

    @Override
    public String baseFormClause() {
        return baseFormClause;
    }

    @Override
    public String alias() {
        return aliasName;
    }

    @Override
    public List<JoinObject> joins() {
        return joins;
    }

    public void addJoinObject(JoinObject.JoinType joinType, FromObject target, Operator operator) {
        this.joins.add(new InnerJoinObject(joinType, target, operator));
    }

    public void addJoinObject(JoinObject.JoinType joinType, FromObject target, String leftFieldName, String rightFieldName) {
        this.joins.add(new InnerJoinObject(joinType, target, Operator.eq(leftFieldName, rightFieldName)));
    }

    record InnerJoinObject(JoinType joinType, FromObject target, Operator operator) implements JoinObject {

        @Override
        public @NonNull JoinObject.JoinType getJoinType() {
            return joinType;
        }

        @Override
        public @NonNull FromObject targetFromObject() {
            return target;
        }

        @Override
        public Operator getOperator() {
            return operator;
        }


    }

}
