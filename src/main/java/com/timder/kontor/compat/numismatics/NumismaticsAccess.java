package com.timder.kontor.compat.numismatics;

import dev.ithundxr.createnumismatics.Numismatics;
import dev.ithundxr.createnumismatics.content.backend.BankAccount;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public final class NumismaticsAccess {

    /**
     * ID of the bank account used as a reserve.
     */
    public static final UUID RESERVE_ID = UUID.nameUUIDFromBytes("createkontor:numismatics_reserve".getBytes(StandardCharsets.UTF_8));

    public enum TakeResult {
        OK,
        NO_ACCOUNT,
        NOT_ENOUGH_COINS,
        RESERVE_FULL
    }

    /**
     * Moves spurs from the bank account into the reserve
     * @param playerId
     * @param spurs
     * @return
     */
    public static TakeResult take(UUID playerId, int spurs) {
        Objects.requireNonNull(playerId, "playerId must not be null.");
        if (spurs <= 0) throw new IllegalArgumentException("spurs must be positive.");

        BankAccount player = Numismatics.BANK.getAccount(playerId);
        if (player == null) {
            return TakeResult.NO_ACCOUNT;
        }
        BankAccount reserve = reserve();
        if (reserve.getBalance() > Integer.MAX_VALUE - spurs) {
            return TakeResult.RESERVE_FULL;
        }
        if (!player.deduct(spurs, false)) {
            return TakeResult.NOT_ENOUGH_COINS;
        }
        reserve.deposit(spurs);
        return TakeResult.OK;
    }

    /**
     * Give spurs back to players from the reserve
     * @param playerId The player ID
     * @param spurs The spurs
     */
    public static void giveBack(UUID playerId, int spurs) {
        Objects.requireNonNull(playerId, "playerId must not be null.");
        if (spurs <= 0) throw new IllegalArgumentException("spurs must be positive.");

        BankAccount player = Numismatics.BANK.getAccount(playerId);
        if (player == null) throw new IllegalStateException("There is no bank account for " + playerId + ".");
        BankAccount reserve = reserve();
        if (!reserve.deduct(spurs, false)) {
            throw new IllegalStateException("The reserve holds fewer than " + spurs + " spurs.");
        }
        player.deposit(spurs);
    }

    public static int reserveBalance() {
        return reserve().getBalance();
    }

    private static BankAccount reserve() {
        return Numismatics.BANK.getOrCreateAccount(RESERVE_ID, BankAccount.Type.PLAYER);
    }
}
