package cn.procsl.ping.boot.jpa.support.query.ast;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class FragmentClause implements Clause {

    final String clauseString;

    @Override
    public String toClauseString() {
        return clauseString;
    }

    @Override
    public String toString() {
        return toClauseString();
    }
}
