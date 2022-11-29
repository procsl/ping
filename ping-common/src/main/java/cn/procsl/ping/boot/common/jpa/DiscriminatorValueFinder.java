package cn.procsl.ping.boot.common.jpa;

import jakarta.persistence.DiscriminatorValue;
import lombok.val;

public interface DiscriminatorValueFinder {

    default String find() {
        val discriminatorValues = this.getClass().getAnnotationsByType(DiscriminatorValue.class);
        if (discriminatorValues.length == 0) {
            return null;
        }
        return discriminatorValues[0].value();
    }

}
