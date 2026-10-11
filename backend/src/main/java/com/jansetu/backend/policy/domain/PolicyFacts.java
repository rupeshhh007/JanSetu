package com.jansetu.backend.policy.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.List;

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

    /**
     * Stores facts as canonical strings paired with their closed fact key. This
     * avoids JSON number coercion and lets a future replay reconstruct the
     * exact value types used during the original evaluation.
     */
    public List<PolicyFactSnapshot> snapshot() {
        return values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new PolicyFactSnapshot(entry.getKey(), serialize(entry.getValue())))
                .toList();
    }

    public static PolicyFacts fromSnapshot(List<PolicyFactSnapshot> snapshot) {
        Builder builder = builder();
        if (snapshot == null) {
            return builder.build();
        }
        for (PolicyFactSnapshot fact : snapshot) {
            if (fact == null || fact.fact() == null || fact.value() == null) {
                throw new IllegalArgumentException("A fact snapshot must include a key and value");
            }
            builder.put(fact.fact(), deserialize(fact));
        }
        return builder.build();
    }

    private static String serialize(Object value) {
        return switch (value) {
            case BigDecimal decimal -> decimal.toPlainString();
            case LocalDate date -> date.toString();
            case Boolean bool -> Boolean.toString(bool);
            case String text -> text;
            default -> throw new IllegalArgumentException("Unsupported policy fact value: " + value.getClass().getName());
        };
    }

    private static Object deserialize(PolicyFactSnapshot fact) {
        try {
            return switch (fact.fact().valueType()) {
                case DECIMAL -> new BigDecimal(fact.value());
                case DATE -> LocalDate.parse(fact.value());
                case BOOLEAN -> Boolean.parseBoolean(fact.value());
                case TEXT -> fact.value();
            };
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid snapshot value for " + fact.fact(), exception);
        }
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
