package cn.procsl.ping.boot.domain.business.dictionary.model;

import cn.procsl.ping.boot.domain.business.utils.StringUtils;
import lombok.*;
import org.springframework.data.annotation.Immutable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.Serializable;

@Getter
@Setter(AccessLevel.PROTECTED)
@ToString
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Immutable
public class Value implements Serializable {

    @Column(length = 100, nullable = false, updatable = false)
    String value;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false, updatable = false)
    Type type;

    public Value(@NonNull Type type, String value) {
        boolean bool = (!Type.blank.equals(type)) && StringUtils.isEmpty(value);
        if (bool) {
            throw new IllegalArgumentException("value can not be blank");
        }
        // test this value
        type.format(value);
        this.type = type;
        this.value = value;

        if (Type.blank.equals(type)) {
            this.value = "";
        }
    }
}
