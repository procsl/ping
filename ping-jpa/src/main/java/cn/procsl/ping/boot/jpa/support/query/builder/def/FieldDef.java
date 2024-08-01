package cn.procsl.ping.boot.jpa.support.query.builder.def;

public interface FieldDef {

    Class<?> getType();

    String getFieldName();

    <T, R> R accept(FieldDefVisitor<R, T> visitor, T args);

}
