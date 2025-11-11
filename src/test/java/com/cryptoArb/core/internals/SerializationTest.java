package com.cryptoArb.core.internals;

import com.cryptoArb.domainOld.CurrencyPair_old;
import com.cryptoArb.domainOld.Exchange_old;
import com.cryptoArb.domainOld.PriceTick_old;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SerializationTest {

    // JUnit will create a temporary directory for us
    @TempDir
    Path tempDir;

    private PriceTick_old testTick;
    private File serializationFile;

    @BeforeEach
    void setUp() {
        // Create the object we want to serialize
        testTick = new PriceTick_old(
                new CurrencyPair_old("BTC", "USD"),
                new Exchange_old("coinbase"),
                Instant.now(),
                new BigDecimal("50000"),
                new BigDecimal("50001")
        );

        // Define the file we'll write to
        // tick.ser is the name of the file
        //.ser extension is used for serialized files
        serializationFile = tempDir.resolve("tick.ser").toFile();
    }

    /**
     * Helper method to serialize (write) the object to a file.
     * This is the first method our test plan requires[cite: 152].
     */
    private void serializeToDisk(PriceTick_old tick) throws IOException {
        // This is the core Java serialization logic
        // we try with resources to ensure proper resource management
        // here we are using try-with-resources statement to ensure that the file and object streams are closed after use
        // FileOutputStream is used to write bytes to a file
        // serializationFile is the file we want to write to
        try (FileOutputStream fileOut = new FileOutputStream(serializationFile);
             // ObjectOutputStream is used to write objects to a file
             //fileOut is the stream we want to write to
             ObjectOutputStream objOut = new ObjectOutputStream(fileOut)) {

            // This line writes the object to the file
            objOut.writeObject(tick);
        }
    }

    /**
     * Helper method to deserialize (read) the object from a file.
     * This is the second method our test plan requires[cite: 152].
     */
    private PriceTick_old deserializeFromDisk() throws IOException, ClassNotFoundException {
        try (FileInputStream fileIn = new FileInputStream(serializationFile);
             ObjectInputStream objIn = new ObjectInputStream(fileIn)) {

            // This line reads the object back
            // objIn is the stream we want to read from
            return (PriceTick_old) objIn.readObject();
        }
    }

    @Test
    @DisplayName("Should serialize and deserialize a PriceTick_old object")
    void shouldSerializeAndDeserializeObject() throws IOException, ClassNotFoundException { // Updated signature

        // --- This is the new test logic ---

        // When: We serialize the object
        serializeToDisk(testTick);

        // And: We deserialize it back
        PriceTick_old deserializedTick = deserializeFromDisk();

        // Then: The deserialized object should be equal to the original
        assertNotNull(deserializedTick);

        // We test field by field because PriceTick_old doesn't have an .equals() method
        assertEquals(testTick.getPair(), deserializedTick.getPair()); // This works because CurrencyPair_old *does* have .equals()
        assertEquals(testTick.getExchange().getId(), deserializedTick.getExchange().getId()); // Exchange_old has no .equals, so we check the ID
        assertEquals(testTick.getTimestamp(), deserializedTick.getTimestamp());
        assertEquals(testTick.getBidPrice(), deserializedTick.getBidPrice());
        assertEquals(testTick.getAskPrice(), deserializedTick.getAskPrice());
    }
}
