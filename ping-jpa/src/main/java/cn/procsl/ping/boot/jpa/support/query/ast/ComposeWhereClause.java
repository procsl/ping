package cn.procsl.ping.boot.jpa.support.query.ast;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class ComposeWhereClause implements WhereClause {


    private final List<NodeClause> clauses = new ArrayList<>();

    public void and(final WhereClause clause) {
        clauses.add(new NodeClause("and", clause));
    }

    public void or(final WhereClause clause) {
        clauses.add(new NodeClause("or", clause));
    }

    public void not(final WhereClause clause) {
        clauses.add(new NodeClause("not", clause));
    }


    @Override
    public String toClauseString() {
        this.clauses.sort(Comparator.comparingInt(NodeClause::getOrder));
        return this.clauses.stream().map(NodeClause::toClauseString).collect(Collectors.joining(" "));
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @RequiredArgsConstructor
    static class NodeClause implements Clause, Order {

        final String logic;
        final WhereClause clause;

        @Override
        public String toClauseString() {
            return logic + " " + clause.toClauseString();
        }

        @Override
        public String toString() {
            return this.toClauseString();
        }

        @Override
        public int getOrder() {
            return clause.getOrder();
        }
    }

}
