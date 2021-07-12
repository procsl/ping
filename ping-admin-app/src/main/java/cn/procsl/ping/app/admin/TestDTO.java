package cn.procsl.ping.app.admin;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Collection;

public class TestDTO {

    String field;

    Collection<@Size() @Min(10) String> list;

    public String getField() {
        return field;
    }

    public void setField(@Min(10) String field) {
        this.field = field;
    }
}

