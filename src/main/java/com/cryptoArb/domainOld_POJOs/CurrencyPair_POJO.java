package com.cryptoArb.domainOld_POJOs;

import java.io.Serializable;

/**
 * The purpose of that requirement was to show I understand the "why" behind modern tools by first building the classic equivalent.
 * <p>
 * What Serialization Demonstrates: It proves I understand how to convert a live Java object's state into a byte stream, which can be persisted to disk or sent over a network. It also forced me to manage the "deep" nature of serialization, as I had to make my entire object graph—PriceTick_POJO, CurrencyPair_old, and Exchange_POJO—implement the Serializable interface. Finally, it taught me the importance of serialVersionUID for versioning, which is critical for maintaining compatibility if the class structure changes later.
 * <p>
 * What are the alternatives? The main alternative, and the one this project will use in its final form (Part 2), is text-based serialization, like JSON or XML.
 * <p>
 * In fact, the first task in this same phase was to build a SimpleJsonSerializer, which does exactly that. It's a fantastic contrast:
 * <p>
 * Java Serialization is binary, efficient, and specific to Java. It's great for Java-to-Java communication but is brittle and not human-readable.
 * <p>
 * JSON Serialization is text-based, human-readable, and platform-independent. It's the standard for modern REST APIs and web services, which is what the final Spring Boot app will use.
 * <p>
 * <p>
 * Another alternative for persistence (not transfer) is, of course, the database, which I used in Phase 5 with the DatabaseService.
 * <p>
 * What if it was not used? If I hadn't used it, I would have missed a key educational goal of the project. I would have only demonstrated the modern "how" (using JSON) without proving I understood the fundamental "why" (the concept of object serialization itself).
 * <p>
 * In summary: I included serialization to demonstrate a fundamental, low-level Java platform feature. It shows I can persist objects directly and understand the trade-offs between that classic approach (binary, Java-specific) and the modern, text-based alternatives (JSON) that I also implemented.
 *
 */
public class CurrencyPair_POJO implements Serializable {

    // 3. Add a version ID
    private static final long serialVersionUID = 1L;

    private final String base;
    private final String quote;

    public CurrencyPair_POJO(String base, String quote) {
        this.base = base;
        this.quote = quote;
    }

    public String getBase() {
        return base;
    }

    public String getQuote() {
        return quote;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CurrencyPair_POJO that = (CurrencyPair_POJO) o;
        return base.equals(that.base) && quote.equals(that.quote);
    }

    @Override
    public int hashCode() {
        int result = base.hashCode();
        result = 31 * result + quote.hashCode();
        return result;
    }
}
