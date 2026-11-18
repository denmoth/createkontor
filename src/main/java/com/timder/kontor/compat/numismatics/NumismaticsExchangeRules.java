package com.timder.kontor.compat.numismatics;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.financial.BookingKind;
import com.timder.kontor.core.company.financial.Money;

import java.math.BigInteger;
import java.util.Objects;

public class NumismaticsExchangeRules {

    private static final BigInteger FULL_BASIS_POINTS = BigInteger.valueOf(10_000);

    /**
     * @param spurs The spurs
     * @param centsPerSpur The value of one spur in cents
     * @return The money: spurs x value x (1 - fee)
     */
    static Money moneyFor(long spurs, long centsPerSpur) {
        if (spurs < 0 || spurs > Integer.MAX_VALUE)
            throw new IllegalArgumentException("spurs must be between 0 and " + Integer.MAX_VALUE + ".");
        if (centsPerSpur < 1 || centsPerSpur > 1_000_000_000L)
            throw new IllegalArgumentException("centsPerSpur must be between 1 and 1000000000.");

        BigInteger exact = BigInteger.valueOf(spurs)
                .multiply(BigInteger.valueOf(centsPerSpur));
        return Money.ofCents(exact.divide(FULL_BASIS_POINTS).longValueExact());
    }

    /**
     * Exchanges money and books it to the player
     * @return The exchanged money
     */
    static Money exchange(
            Company company, long spurs, long day, String reference, long centsPerSpur) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(reference, "reference must not be null.");
        if (day < 0) throw new IllegalArgumentException("day must not be negative.");

        Money credit = moneyFor(spurs, centsPerSpur);
        if (!credit.isPositive()) {
            throw new IllegalStateException("Exchanging " + spurs + " spurs gives no credit.");
        }
        company.account().book(day, BookingKind.EXCHANGE_IN, credit, reference);
        return credit;
    }
}
