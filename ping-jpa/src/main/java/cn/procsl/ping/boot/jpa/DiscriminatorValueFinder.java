package cn.procsl.ping.boot.jpa;

import lombok.val;

import javax.persistence.DiscriminatorValue;

public interface DiscriminatorValueFinder {

    default String find() {
        val discriminatorValues = this.getClass().getAnnotationsByType(DiscriminatorValue.class);
        if (discriminatorValues.length == 0) {
            return null;
        }
        return discriminatorValues[0].value();
    }

}
