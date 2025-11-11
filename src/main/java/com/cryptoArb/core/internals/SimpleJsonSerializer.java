package com.cryptoArb.core.internals;

import java.lang.reflect.Field;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple serializer that uses reflection to convert an object
 * into a JSON string.
 *
 * This fulfills Phase 9 of the project plan.
 *
 *
 */
public class SimpleJsonSerializer {

    /**
     * Converts an object into a JSON string.
     * This method uses reflection to access both public and private fields.
     * It formats each field as "name":value, where:
     * - "name" is the field name
     * - "value" is the field's value, as a string
     *   - If the value is a Number, it is not quoted
     *   - Otherwise, it is quoted
     * The method joins all entries with a comma and wraps the result in braces.
     * @param o The object to serialize
     * @return The JSON string representation of the object
     */
    public String serialize(Object o) {
        // 1. Get the class of the object (e.g., PriceTick.class)
        Class<?> objectClass = o.getClass();

        // 2. Get all fields *declared* in that class, even private ones
        // for example if we have a PriceTick object with fields pair, exchange, timestamp, bidPrice, askPrice
        // getDeclaredFields() vs. getFields(): We must use getDeclaredFields().
        // The fields on your PriceTick record are private, and getFields() only returns public fields.
        // getDeclaredFields() returns all fields, regardless of visibility.
        Field[] fields = objectClass.getDeclaredFields();

        // 3. Use a StringBuilder to build the JSON string
        // We will collect entries like "\"fieldName\":\"value\""
        String body = Stream.of(fields)
                .map(field -> {
                    try {
                        // 4. THIS IS THE KEY: We must make private fields accessible
                        // The fields in a 'record' are private and final.
                        // without this, we will get IllegalAccessException
                        field.setAccessible(true);

                        // 5. Get the field's name and its value from the object 'o'
                        // e.g., "pair", "exchange", "timestamp", "bidPrice", "askPrice"
                        String name = field.getName();
                        // example for PriceTick object, field.get(o), value will be pair, exchange, timestamp, bidPrice, askPrice
                        Object value = field.get(o);

                        // 6. Format as "name":value
                        // This is the minimal logic to pass our test
                        if (value instanceof Number) {
                            // Don't quote numbers (like BigDecimal)
                            return "\"" + name + "\":" + value.toString();
                        } else {
                            // Do quote strings and other objects
                            return "\"" + name + "\":\"" + value.toString() + "\"";
                        }

                    } catch (IllegalAccessException e) {
                        // This would happen if setAccessible(true) failed
                        throw new RuntimeException("Could not access field: " + field.getName(), e);
                    }
                })
                .collect(Collectors.joining(",")); // Join all entries with a comma

        // 7. Wrap the body in braces
        return "{" + body + "}";
    }
}
