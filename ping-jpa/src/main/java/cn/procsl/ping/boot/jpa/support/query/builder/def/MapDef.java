package cn.procsl.ping.boot.jpa.support.query.builder.def;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

public interface MapDef extends FieldDef {

    @Override
    default <T, R> R accept(@NonNull FieldDefVisitor<R, T> visitor, T args) {
        return visitor.visitMapField(this, args);
    }

    @Getter
    @AllArgsConstructor
    class InnerDef implements MapDef {
        String fieldName;
        Class<?> type;
    }

}
