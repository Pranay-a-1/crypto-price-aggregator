package com.cryptoArb.core.internals;

import com.cryptoArb.javaImpl.domain_POJOs.CurrencyPair_POJO;
import com.cryptoArb.javaImpl.domain_POJOs.Exchange_POJO;
import com.cryptoArb.javaImpl.domain_POJOs.PriceTick_POJO;
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

    private PriceTick_POJO testTick;
    private File serializationFile;

    @BeforeEach
    void setUp() {
        // Create the object we want to serialize
        testTick = new PriceTick_POJO(
                new CurrencyPair_POJO("BTC", "USD"),
                new Exchange_POJO("coinbase"),
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
     * This is the first method our test plan requires
     */
    private void serializeToDisk(PriceTick_POJO tick) throws IOException {
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
     * This is the second method our test plan requires
     */
    private PriceTick_POJO deserializeFromDisk() throws IOException, ClassNotFoundException {
        try (FileInputStream fileIn = new FileInputStream(serializationFile);
             ObjectInputStream objIn = new ObjectInputStream(fileIn)) {

            // This line reads the object back
            // objIn is the stream we want to read from
            return (PriceTick_POJO) objIn.readObject();
        }
    }

    @Test
    @DisplayName("Should serialize and deserialize a PriceTick_POJO object")
    void shouldSerializeAndDeserializeObject() throws IOException, ClassNotFoundException { // Updated signature

        // --- This is the new test logic ---

        // When: We serialize the object
        serializeToDisk(testTick);

        // And: We deserialize it back
        PriceTick_POJO deserializedTick = deserializeFromDisk();

        // Then: The deserialized object should be equal to the original
        assertNotNull(deserializedTick);

        // We test field by field because PriceTick_POJO doesn't have an .equals() method
        assertEquals(testTick.getPair(), deserializedTick.getPair()); // This works because CurrencyPair_POJO *does* have .equals()
        assertEquals(testTick.getExchange().getId(), deserializedTick.getExchange().getId()); // Exchange_POJO has no .equals, so we check the ID
        assertEquals(testTick.getTimestamp(), deserializedTick.getTimestamp());
        assertEquals(testTick.getBidPrice(), deserializedTick.getBidPrice());
        assertEquals(testTick.getAskPrice(), deserializedTick.getAskPrice());
    }
}
