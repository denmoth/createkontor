package com.timder.kontor.compat.numismatics;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;
import java.util.Objects;
import java.util.UUID;

public final class NumismaticsAccess {

    /**
     * @param playerId The player ID
     * @return The spurs in the bank account of the player
     */
    public static int balanceOf(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId must not be null.");
        BankAccount account = Numismatics.BANK.getAccount(playerId);
        return account == null ? 0 : account.getBalance();
    }

    /**
     * @param playerId The placer ID
     * @param spurs The spurs
     * @return False if the placer has fewer spurs, nothing changed
     */
    public static boolean take(UUID playerId, int spurs) {
        Objects.requireNonNull(playerId, "playerId must not be null.");
        if (spurs <= 0) throw new IllegalArgumentException("spurs must be positive.");

        BankAccount account = Numismatics.BANK.getAccount(playerId);
        return account != null && account.deduct(spurs, false);
    }

    /**
     * Adds spurs to the bank account of a player
     * @param playerId The player ID
     * @param spurs The spurs
     * @return False if the balance would be above the max amount, nothing changes then
     */
    public static boolean give(UUID playerId, int spurs) {
        Objects.requireNonNull(playerId, "playerId must not be null.");
        if (spurs <= 0) throw new IllegalArgumentException("spurs must be positive.");

        BankAccount account = Numismatics.BANK.getOrCreateAccount(playerId, BankAccount.Type.PLAYER);
        if (account.getBalance() > Integer.MAX_VALUE - spurs) return false;
        account.deposit(spurs);
        return true;
    }
}
