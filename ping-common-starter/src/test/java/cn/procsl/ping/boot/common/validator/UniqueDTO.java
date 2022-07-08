package cn.procsl.ping.boot.common.validator;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Data
@RequiredArgsConstructor
@UniqueField(entity = Unique.class, fieldName = "column")
public class UniqueDTO extends AbstractPersistable<Long> {

    final String column;

    public UniqueDTO(Long id, String column) {
        this.setId(id);
        this.column = column;
    }
}
