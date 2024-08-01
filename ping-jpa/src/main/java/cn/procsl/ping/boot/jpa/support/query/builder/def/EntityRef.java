package cn.procsl.ping.boot.jpa.support.query.builder.def;

public interface EntityRef {

    /**
     * 实体对应字段
     */
    String getEntityFieldName();

    /**
     * 实体别名
     */
    String getEntityAlias();

}
