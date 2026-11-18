package com.timder.kontor.compat.numismatics;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.financial.BookingKind;
import com.timder.kontor.core.company.financial.Money;
import java.util.Objects;

public class NumismaticsExchangeRules {

    /**
     * @param spurs The spurs
     * @param centsPerSpur The value of one spur in cents
     * @return The money
     */
    static Money moneyFor(long spurs, long centsPerSpur) {
        if (spurs < 0 || spurs > Integer.MAX_VALUE)
            throw new IllegalArgumentException("spurs must be between 0 and " + Integer.MAX_VALUE + ".");
        if (centsPerSpur < 1 || centsPerSpur > 1_000_000_000L)
            throw new IllegalArgumentException("centsPerSpur must be between 1 and 1000000000.");
        return Money.ofCents(Math.multiplyExact(spurs, centsPerSpur));
    }

    /**
     * Exchanges money and books it to the player
     * @return The exchanged money
     */
    static Money exchange(Company company, long spurs, long day, String reference, long centsPerSpur) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(reference, "reference must not be null.");
        if (day < 0) throw new IllegalArgumentException("day must not be negative.");

        Money credit = moneyFor(spurs, centsPerSpur);
        if (!credit.isPositive()) {
            throw new IllegalStateException("Exchanging " + spurs + " spurs gives no credit.");
        }
        company.account().book(day, BookingKind.EXCHANGE, credit, reference);
        return credit;
    }

    static boolean canBuyBack(Company company, Money cost) {
        Objects.requireNonNull(company, "company must not be null.");
        Objects.requireNonNull(cost, "cost must not be null.");
        return cost.isPositive() && company.isOperational() && company.account().canAfford(cost, Money.ZERO);
    }

    static void buyBack(Company company, long day, String reference, Money cost) {
        Objects.requireNonNull(reference, "reference must not be null.");
        if (day < 0) throw new IllegalArgumentException("day must not be negative.");
        if (!canBuyBack(company, cost)) throw new IllegalStateException("The company cannot pay " + cost + ".");
        company.account().book(day, BookingKind.EXCHANGE, cost.negate(), reference);
    }
}
