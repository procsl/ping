package cn.procsl.ping.boot.jpa.support;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.lang.NonNullApi;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

public class TupleConverter implements Converter<Object, Object> {

    private final ReturnedType type;


    public TupleConverter(ReturnedType type) {

        Assert.notNull(type, "Returned type must not be null!");

        this.type = type;
    }


    @Override
    public Object convert(Object source) {

        if (!(source instanceof Tuple tuple)) {
            return source;
        }

        List<TupleElement<?>> elements = tuple.getElements();

        if (elements.size() == 1) {

            Object value = tuple.get(elements.get(0));

            if (type.isInstance(value) || value == null) {
                return value;
            }
        }

        return new TupleConverter.TupleBackedMap(tuple);
    }


    private static class TupleBackedMap implements Map<String, Object> {

        private static final String UNMODIFIABLE_MESSAGE = "A TupleBackedMap cannot be modified.";

        private final Tuple tuple;

        TupleBackedMap(Tuple tuple) {
            this.tuple = tuple;
        }

        @Override
        public int size() {
            return tuple.getElements().size();
        }

        @Override
        public boolean isEmpty() {
            return tuple.getElements().isEmpty();
        }


        @Override
        public boolean containsKey(Object key) {

            try {
                tuple.get((String) key);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        @Override
        public boolean containsValue(Object value) {
            return Arrays.asList(tuple.toArray()).contains(value);
        }


        @Override
        @Nullable
        public Object get(Object key) {

            if (!(key instanceof String)) {
                return null;
            }

            try {
                return tuple.get((String) key);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        @Override
        public Object put(String key, Object value) {
            throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
        }

        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
        }

        @Override
        public void putAll(Map<? extends String, ?> m) {
            throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
        }

        @Override
        public Set<String> keySet() {

            return tuple.getElements().stream() //
                    .map(TupleElement::getAlias) //
                    .collect(Collectors.toSet());
        }

        @Override
        public Collection<Object> values() {
            return Arrays.asList(tuple.toArray());
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {

            return tuple.getElements().stream() //
                    .map(e -> new HashMap.SimpleEntry<String, Object>(e.getAlias(), tuple.get(e))) //
                    .collect(Collectors.toSet());
        }
    }
}
