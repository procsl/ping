package cn.procsl.ping.boot.jpa.support.query.ast;

public interface Variable extends Expression {

    String getName();

    Object getValue();

}
