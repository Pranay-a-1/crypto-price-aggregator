package com.cryptoArb.core.datastructures;

import java.util.Objects;

/**
 * A from-scratch implementation of a simple HashMap to demonstrate
 * understanding of core data structures.
 *
 * This fulfills Phase 3 of the project plan.
 */
public class MyHashMap<K, V> {

    // 1. Our internal Entry class
    private static class Entry<K, V> {
        final K key;
        V value;
        Entry<K, V> next; // For handling collisions (linked list)

        Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    // 2. The array of buckets (the "table")
    private static final int DEFAULT_CAPACITY = 16;
    private Entry<K, V>[] buckets;
    private int size = 0; // Number of key-value pairs

    // 3. We will add a constructor to initialize the map.
    /**
     * Initializes the map
     */
    public MyHashMap() {
        // We can't create a generic array directly, so we create an Entry array
        // and cast it. This is a common practice.
        this.buckets = (Entry<K, V>[]) new Entry[DEFAULT_CAPACITY];
    }

    /**
     * A helper method to find the correct bucket index for a key.
     * @param key The key to hash
     * @return The index (0 to capacity-1) in our 'buckets' array
     */
    private int getBucketIndex(K key) {
        if (key == null) {
            return 0; // Or handle as a special case
        }
        // Get the hash code and ensure it's non-negative
        int hashCode = key.hashCode();
        int positiveHash = Math.abs(hashCode);

        // Use modulo to fit it into our array size
        // Note: This can lead to uneven distribution if capacity is not prime
        // but is acceptable for this simple implementation.
        // for example, we can use buckets.length which is always a power of two in this implementation.
        // This is a simple approach; more advanced implementations use techniques like bit masking.
        // For this implementation, we will use modulo operation.
        // for example , given hashcode 123456 and buckets.length 16 => 123456 % 16 = 0
        return positiveHash % buckets.length;
    }

    // 4. We will implement the put() method.
    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key The key with which the specified value is to be associated
     * @param value The value to be associated with the specified key
     * @return The previous value associated with key, or null if there was
     * no mapping for key.
     */
    public V put(K key, V value) {
        // 1. Find the bucket
        int index = getBucketIndex(key);

        // 2. Get the first entry in that bucket (it's a linked list)
        Entry<K, V> current = buckets[index];

        // 3. Search for the key in the linked list
        while (current != null) {
            // We use Objects.equals for safe comparison (handles null keys)
            if (Objects.equals(current.key, key)) {
                // Key found! Update the value.
                V oldValue = current.value;
                current.value = value;
                // Return the *old* value, as per the Map contract
                return oldValue;
            }
            // Move to the next entry in the list
            current = current.next;
        }

        // 4. Key not found. This is a new entry.
        // We add it to the *front* of the linked list for simplicity.

        // Get the existing first entry (which will be our new entry's 'next')
        Entry<K, V> existingEntry = buckets[index];

        // Create the new Entry, pointing to the old first entry
        Entry<K, V> newEntry = new Entry<>(key, value, existingEntry);

        // Point the bucket to our new Entry as the new head of the list
        buckets[index] = newEntry;

        size++; // Don't forget to increment the size!

        // As per the Map contract, return null since there was no old value
        return null;
    }

    // 5. We will implement the get() method.
    /**
     * Returns the value to which the specified key is mapped,
     * or null if this map contains no mapping for the key.
     *
     * @param key The key whose associated value is to be returned
     * @return The value, or null if the key was not found.
     */
    public V get(K key) {
        // 1. Find the bucket
        int index = getBucketIndex(key);

        // 2. Get the head of the linked list
        Entry<K, V> current = buckets[index];

        // 3. Traverse the list in that bucket
        while (current != null) {
            // 4. Check for a match
            if (Objects.equals(current.key, key)) {
                // Key found! Return the value.
                return current.value;
            }
            // Move to the next entry
            current = current.next;
        }

        // 5. Key not found after checking the whole list
        return null;
    }

    /**
     * Returns the number of key-value mappings in this map.
     * @return The number of entries
     */
    public int size() {
        return this.size;
    }


}