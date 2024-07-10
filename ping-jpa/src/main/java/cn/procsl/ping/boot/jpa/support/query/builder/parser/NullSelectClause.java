package cn.procsl.ping.boot.jpa.support.query.builder.parser;

final public class NullSelectClause extends StringSelectClause {
    public NullSelectClause(int index) {
        super(index, null, null);
    }

}
