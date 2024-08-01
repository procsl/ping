package cn.procsl.ping.boot.jpa.support.query.builder.def;

public interface FieldDefVisitor<R, T> {


    R visitSimpleField(SimpleDef def, T args);

    R visitCollectionField(CollectionDef def, T args);

    R visitPojoField(PojoDef def, T args);

    R visitMapField(MapDef def, T args);

    R visitEntityField(EntityRef def, T args);

}
