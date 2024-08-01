package cn.procsl.ping.boot.jpa.support.query.builder.def;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

public interface CollectionDef extends FieldDef {

    FieldDef getContent();

    @Override
    default <T, R> R accept(@NonNull FieldDefVisitor<R, T> visitor, T args) {
        return visitor.visitCollectionField(this, args);
    }


    @Getter
    @AllArgsConstructor
    class InnerDef implements CollectionDef {
        String fieldName;
        Class<?> type;
        FieldDef content;
    }

}
