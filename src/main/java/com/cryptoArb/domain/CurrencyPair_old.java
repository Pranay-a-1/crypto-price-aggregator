package com.cryptoArb.domain;

public class CurrencyPair_old {

    private final String base;
    private final String quote;

    public CurrencyPair_old(String base, String quote) {
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

        CurrencyPair_old that = (CurrencyPair_old) o;
        return base.equals(that.base) && quote.equals(that.quote);
    }

    @Override
    public int hashCode() {
        int result = base.hashCode();
        result = 31 * result + quote.hashCode();
        return result;
    }
}
