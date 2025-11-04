package com.cryptoArb.core.datastructures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MyHashMapTest {

    private MyHashMap<String, Integer> map;

    @BeforeEach
    void setUp() {
        // This runs before every @Test method
        map = new MyHashMap<>();
    }

    @Test
    @DisplayName("Should put a new key-value pair and get it")
    void shouldPutAndGetNewEntry() {
        // --- Arrange ---
        // We have our empty 'map' from setUp()
        String key = "BTC";
        Integer value = 50000;

        // --- Act ---
        // Add the key and value to the map
        map.put(key, value);

        // --- Assert ---
        // Verify the value was stored correctly
        Integer retrievedValue = map.get(key);
        assertEquals(value, retrievedValue, "The value retrieved should match the value put in");

        // Check that the map's size is now 1
        assertEquals(1, map.size(), "Map size should be 1 after adding one entry");
    }


    @Test
    @DisplayName("Should update the value of an existing key")
    void shouldUpdateExistingEntry() {
        // --- Arrange ---
        String key = "BTC";
        Integer initialValue = 50000;
        Integer newValue = 50001;

        // Add the initial key-value pair
        map.put(key, initialValue);

        // --- Act ---
        // Update the map with the new value and capture the returned old value
        Integer returnedOldValue = map.put(key, newValue);

        // --- Assert ---
        // 1. Verify the new value is stored
        Integer retrievedValue = map.get(key);
        assertEquals(newValue, retrievedValue, "The value should be updated to the new value");

        // 2. Verify the size is still 1
        assertEquals(1, map.size(), "Map size should remain 1 after an update");

        // 3. Verify the correct old value was returned
        assertEquals(initialValue, returnedOldValue, "The put method should return the old value");
    }


    // --- NEW TEST ---

    /**
     * A simple helper class for our collision test.
     * We can force its hashCode to be whatever we want.
     */
    private static class CollidingKey {
        private final String id;
        private final int hashCode;

        public CollidingKey(String id, int hashCode) {
            this.id = id;
            this.hashCode = hashCode;
        }

        @Override
        public int hashCode() {
            // We return the forced hash code
            return this.hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            // Standard equals check
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            CollidingKey that = (CollidingKey) obj;
            return Objects.equals(id, that.id);
        }
    }

    @Test
    @DisplayName("Should handle collisions in the same bucket")
    void shouldHandleCollisions() {
        // --- Arrange ---
        // We need a map that uses our new CollidingKey
        MyHashMap<CollidingKey, String> collisionMap = new MyHashMap<>();

        // 1. Create two keys that are NOT equal
        // 2. But have hash codes that collide in a 16-bucket map

        // key1: hashCode = 1. (Index will be 1 % 16 = 1)
        CollidingKey key1 = new CollidingKey("key1", 1);
        String value1 = "Value for Key 1";

        // key2: hashCode = 17. (Index will be 17 % 16 = 1)
        CollidingKey key2 = new CollidingKey("key2", 17);
        String value2 = "Value for Key 2";

        // --- Act ---
        // Put both colliding keys into the map
        collisionMap.put(key1, value1);
        collisionMap.put(key2, value2);

        // --- Assert ---
        // 1. Verify size is 2 (proves no overwrite)
        assertEquals(2, collisionMap.size(), "Map size should be 2, proving no overwrite occurred");

        // 2. Verify both keys can be retrieved
        assertEquals(value1, collisionMap.get(key1), "Should retrieve the correct value for key1");
        assertEquals(value2, collisionMap.get(key2), "Should retrieve the correct value for key2");
    }
}