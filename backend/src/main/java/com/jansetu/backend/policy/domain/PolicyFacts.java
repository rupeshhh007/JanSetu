package com.jansetu.backend.policy.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Immutable, typed facts supplied to a policy evaluation. */
public final class PolicyFacts {
    private final Map<FactKey, Object> values;

    private PolicyFacts(Map<FactKey, Object> values) {
        this.values = Collections.unmodifiableMap(new EnumMap<>(values));
    }

    public Optional<Object> value(FactKey key) {
        return Optional.ofNullable(values.get(key));
    }

    public Optional<BigDecimal> decimal(FactKey key) {
        return value(key).filter(BigDecimal.class::isInstance).map(BigDecimal.class::cast);
    }

    public Optional<LocalDate> date(FactKey key) {
        return value(key).filter(LocalDate.class::isInstance).map(LocalDate.class::cast);
    }

    public Optional<Boolean> bool(FactKey key) {
        return value(key).filter(Boolean.class::isInstance).map(Boolean.class::cast);
    }

    public Optional<String> text(FactKey key) {
        return value(key).filter(String.class::isInstance).map(String.class::cast);
    }

    public Map<FactKey, Object> asMap() {
        return values;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<FactKey, Object> values = new EnumMap<>(FactKey.class);

        public Builder put(FactKey key, Object value) {
            Objects.requireNonNull(key, "fact key is required");
            if (value == null) {
                values.remove(key);
                return this;
            }
            Object normalized = normalize(key, value);
            values.put(key, normalized);
            return this;
        }

        public PolicyFacts build() {
            return new PolicyFacts(values);
        }

        private Object normalize(FactKey key, Object value) {
            return switch (key.valueType()) {
                case DECIMAL -> requireType(key, value, BigDecimal.class);
                case DATE -> requireType(key, value, LocalDate.class);
                case BOOLEAN -> requireType(key, value, Boolean.class);
                case TEXT -> value instanceof Enum<?> enumValue
                        ? enumValue.name()
                        : requireType(key, value, String.class).trim();
            };
        }

        private <T> T requireType(FactKey key, Object value, Class<T> expectedType) {
            if (!expectedType.isInstance(value)) {
                throw new IllegalArgumentException(key + " must use a " + expectedType.getSimpleName() + " value");
            }
            return expectedType.cast(value);
        }
    }
}
