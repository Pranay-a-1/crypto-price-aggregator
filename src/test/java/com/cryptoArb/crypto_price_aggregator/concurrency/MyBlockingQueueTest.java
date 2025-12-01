package com.cryptoArb.crypto_price_aggregator.concurrency;

import com.cryptoArb.crypto_price_aggregator.domain.PriceTick;
import com.cryptoArb.crypto_price_aggregator.domain.CurrencyPair;
import com.cryptoArb.crypto_price_aggregator.domain.Exchange;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for MyBlockingQueue - demonstrates manual synchronization with
 * wait/notify.
 * 
 * Learning Objectives:
 * - Understand synchronized keyword for mutual exclusion
 * - Master wait/notify mechanism for thread coordination
 * - Experience producer-consumer pattern with bounded buffer
 * - Debug deadlock scenarios (intentionally triggered in tests)
 */
@DisplayName("MyBlockingQueue - Thread-Safe Bounded Buffer Tests")
class MyBlockingQueueTest {

    private MyBlockingQueue<PriceTick> queue;
    private static final int QUEUE_CAPACITY = 5;

    @BeforeEach
    void setUp() {
        queue = new MyBlockingQueue<>(QUEUE_CAPACITY);
    }

    @Test
    @DisplayName("Given empty queue, when take called, then should block until item available")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void shouldBlockTakeOnEmptyQueue() throws InterruptedException {
        // GIVEN: Empty queue
        assertTrue(queue.isEmpty(), "Queue should start empty");

        CountDownLatch producerLatch = new CountDownLatch(1);
        List<PriceTick> consumedItems = new ArrayList<>();

        // WHEN: Consumer thread tries to take before producer adds
        Thread consumerThread = new Thread(() -> {
            try {
                // This should block until producer adds an item
                PriceTick tick = queue.take();
                consumedItems.add(tick);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer-Thread");

        consumerThread.start();

        // Give consumer time to block on empty queue
        Thread.sleep(200);

        // Producer adds item after consumer is waiting
        Thread producerThread = new Thread(() -> {
            try {
                PriceTick tick = createTestPriceTick("Binance", "BTC", "USD", "50000");
                queue.put(tick);
                producerLatch.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer-Thread");

        producerThread.start();

        // THEN: Consumer should unblock and consume the item
        producerThread.join(1000);
        consumerThread.join(1000);

        assertEquals(1, consumedItems.size(), "Consumer should have received exactly 1 item");
        assertEquals("Binance", consumedItems.get(0).exchange().getDisplayName(),
                "Consumed item should match produced item");
    }

    @Test
    @DisplayName("Given full queue, when put called, then should block until space available")
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void shouldBlockPutOnFullQueue() throws InterruptedException {
        // GIVEN: Queue filled to capacity
        for (int i = 0; i < QUEUE_CAPACITY; i++) {
            queue.put(createTestPriceTick("Exchange" + i, "BTC", "USD", "50000"));
        }
        assertTrue(queue.isFull(), "Queue should be full");

        AtomicInteger producedCount = new AtomicInteger(0);

        // WHEN: Producer tries to add to full queue
        Thread producerThread = new Thread(() -> {
            try {
                // This should block until consumer makes space
                queue.put(createTestPriceTick("Binance", "BTC", "USD", "51000"));
                producedCount.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer-Thread");

        producerThread.start();

        // Give producer time to block on full queue
        Thread.sleep(200);
        assertEquals(0, producedCount.get(), "Producer should be blocked, nothing added yet");

        // Consumer removes one item to make space
        Thread consumerThread = new Thread(() -> {
            try {
                queue.take(); // Free up one slot
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer-Thread");

        consumerThread.start();

        // THEN: Producer should unblock and add the item
        producerThread.join(1000);
        consumerThread.join(1000);

        assertEquals(1, producedCount.get(), "Producer should have successfully added item");
        assertTrue(queue.isFull(), "Queue should be full again after producer added item");
    }

    @Test
    @DisplayName("Given multiple producers and consumers, when operating concurrently, then no items lost")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void shouldHandleConcurrentProducersAndConsumers() throws InterruptedException {
        // GIVEN: 3 producers, 2 consumers, 10 items per producer
        int numProducers = 3;
        int numConsumers = 2;
        int itemsPerProducer = 10;
        int expectedTotal = numProducers * itemsPerProducer;

        CountDownLatch producersLatch = new CountDownLatch(numProducers);
        CountDownLatch consumersLatch = new CountDownLatch(numConsumers);
        List<PriceTick> consumedItems = new ArrayList<>();

        // WHEN: Start producers
        for (int p = 0; p < numProducers; p++) {
            final int producerId = p;
            Thread producer = new Thread(() -> {
                try {
                    for (int i = 0; i < itemsPerProducer; i++) {
                        PriceTick tick = createTestPriceTick(
                                "Producer" + producerId,
                                "BTC",
                                "USD",
                                String.valueOf(50000 + i));
                        queue.put(tick);
                        Thread.sleep(10); // Simulate work
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    producersLatch.countDown();
                }
            }, "Producer-" + p);
            producer.start();
        }

        // Start consumers
        for (int c = 0; c < numConsumers; c++) {
            Thread consumer = new Thread(() -> {
                try {
                    // Each consumer tries to consume half the total items
                    for (int i = 0; i < expectedTotal / numConsumers; i++) {
                        PriceTick tick = queue.take();
                        synchronized (consumedItems) {
                            consumedItems.add(tick);
                        }
                        Thread.sleep(15); // Simulate processing
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    consumersLatch.countDown();
                }
            }, "Consumer-" + c);
            consumer.start();
        }

        // THEN: All producers should complete
        assertTrue(producersLatch.await(3, TimeUnit.SECONDS),
                "All producers should complete within timeout");

        // All consumers should complete
        assertTrue(consumersLatch.await(3, TimeUnit.SECONDS),
                "All consumers should complete within timeout");

        // Verify no items lost
        assertEquals(expectedTotal, consumedItems.size(),
                "All produced items should be consumed exactly once");
    }

    @Test
    @DisplayName("Given queue at capacity, when size checked, then should return capacity")
    void shouldReturnCorrectSize() throws InterruptedException {
        // GIVEN: Empty queue
        assertEquals(0, queue.size(), "Initial size should be 0");

        // WHEN: Add 3 items
        for (int i = 0; i < 3; i++) {
            queue.put(createTestPriceTick("Exchange" + i, "BTC", "USD", "50000"));
        }

        // THEN
        assertEquals(3, queue.size(), "Size should be 3 after adding 3 items");
        assertFalse(queue.isEmpty(), "Queue should not be empty");
        assertFalse(queue.isFull(), "Queue should not be full (capacity is 5)");
    }

    @Test
    @DisplayName("Given queue with items, when take called, then should return items in FIFO order")
    void shouldMaintainFIFOOrder() throws InterruptedException {
        // GIVEN: Queue with 3 items in specific order (using different currency pairs
        // to verify FIFO)
        PriceTick tick1 = createTestPriceTick("First", "BTC", "USD", "50000");
        PriceTick tick2 = createTestPriceTick("Second", "ETH", "USD", "3000");
        PriceTick tick3 = createTestPriceTick("Third", "XRP", "EUR", "1");

        queue.put(tick1);
        queue.put(tick2);
        queue.put(tick3);

        // WHEN: Take items
        PriceTick result1 = queue.take();
        PriceTick result2 = queue.take();
        PriceTick result3 = queue.take();

        // THEN: Should return in FIFO order (verify by currency pair)
        assertEquals("BTC", result1.pair().base(), "First item should be BTC-USD");
        assertEquals("USD", result1.pair().quote(), "First item should be BTC-USD");

        assertEquals("ETH", result2.pair().base(), "Second item should be ETH-USD");
        assertEquals("USD", result2.pair().quote(), "Second item should be ETH-USD");

        assertEquals("XRP", result3.pair().base(), "Third item should be XRP-EUR");
        assertEquals("EUR", result3.pair().quote(), "Third item should be XRP-EUR");

        assertTrue(queue.isEmpty(), "Queue should be empty after taking all items");
    }

    @Test
    @DisplayName("Given empty queue, when isEmpty called, then should return true")
    void shouldDetectEmptyQueue() {
        // GIVEN & WHEN & THEN
        assertTrue(queue.isEmpty(), "New queue should be empty");
        assertEquals(0, queue.size(), "Size should be 0");
    }

    @Test
    @DisplayName("Given full queue, when isFull called, then should return true")
    void shouldDetectFullQueue() throws InterruptedException {
        // GIVEN: Fill queue to capacity
        for (int i = 0; i < QUEUE_CAPACITY; i++) {
            queue.put(createTestPriceTick("Exchange" + i, "BTC", "USD", "50000"));
        }

        // WHEN & THEN
        assertTrue(queue.isFull(), "Queue at capacity should be full");
        assertEquals(QUEUE_CAPACITY, queue.size(), "Size should equal capacity");
    }

    // Helper method to create test PriceTicks
    private PriceTick createTestPriceTick(String exchangeName, String base, String quote, String price) {
        // Map exchange name to Exchange enum
        Exchange exchange = switch (exchangeName) {
            case "Binance" -> Exchange.BINANCE;
            case "Coinbase" -> Exchange.COINBASE;
            case "Kraken" -> Exchange.KRAKEN;
            default -> Exchange.MOCK; // Default to MOCK for test exchanges
        };

        CurrencyPair pair = new CurrencyPair(base, quote);
        BigDecimal priceValue = new BigDecimal(price);
        BigDecimal bid = priceValue.multiply(new BigDecimal("0.999")); // bid slightly lower
        BigDecimal ask = priceValue.multiply(new BigDecimal("1.001")); // ask slightly higher

        // Correct constructor order: (CurrencyPair, Exchange, bid, ask, timestamp)
        return new PriceTick(
                pair,
                exchange,
                bid,
                ask,
                Instant.now());
    }
}
