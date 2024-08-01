package cn.procsl.ping.boot.jpa.support.query.builder.def;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

public interface SimpleDef extends FieldDef {

    EntityRef getEntityRef();


    @Override
    default <T, R> R accept(@NonNull FieldDefVisitor<R, T> visitor, T args) {
        return visitor.visitSimpleField(this, args);
    }

    @Getter
    @AllArgsConstructor
    class InnerDef implements SimpleDef {
        String fieldName;
        Class<?> type;
        EntityRef entityRef;
    }


}
