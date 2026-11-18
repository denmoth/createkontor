package com.timder.kontor.compat.numismatics;

import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.EconomySavedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

public class NumismaticsExchange {

    public static final String NUMISMATICS_MOD_ID = "numismatics";

    public enum Status {
        OK,
        NOT_AVAILABLE,
        NO_COMPANY,
        INVALID_AMOUNT,
        TOO_SMALL,
        NO_ACCOUNT,
        NOT_ENOUGH_COINS,
        RESERVE_FULL
    }

    public record Result(Status status, Money credited) {
        static Result of(Status status) {
            return new Result(status, Money.ZERO);
        }
    }

    /**
     * @return True, if Create: Numismatics is installed
     */
    public static boolean isAvailable() {
        return ModList.get().isLoaded(NUMISMATICS_MOD_ID);
    }

    public static Result exchange(ServerPlayer player, int spurs) {
        if (!isAvailable()) {
            return Result.of(Status.NOT_AVAILABLE);
        }

        MinecraftServer server = player.getServer();
        if (server == null) return Result.of(Status.NOT_AVAILABLE);
        CompanySavedData companyData = CompanySavedData.get(server);
        Company company = companyData.getRegistry().companyOf(player.getUUID()).orElse(null);
        if (company == null) return Result.of(Status.NO_COMPANY);

        long centsPerSpur = Money.fromDollar(NumismaticsConfig.DOLLARS_PER_SPUR.get()).cents();
        if (spurs <= 0) return Result.of(Status.INVALID_AMOUNT);
        if (!NumismaticsExchangeRules.moneyFor(spurs, centsPerSpur).isPositive())
            return Result.of(Status.TOO_SMALL);

        NumismaticsAccess.TakeResult taken = NumismaticsAccess.take(player.getUUID(), spurs);
        switch (taken) {
            case NO_ACCOUNT:
                return Result.of(Status.NO_ACCOUNT);
            case NOT_ENOUGH_COINS:
                return Result.of(Status.NOT_ENOUGH_COINS);
            case RESERVE_FULL:
                return Result.of(Status.RESERVE_FULL);
            case OK:
                break;
        }

        try {
            long day = EconomySavedData.get(server).getEconomy().currentDay();
            Money credited = NumismaticsExchangeRules.exchange(company, spurs, day, spurs + " spurs", centsPerSpur);
            companyData.setDirty();
            return new Result(Status.OK, credited);
        } catch (RuntimeException e) {
            NumismaticsAccess.giveBack(player.getUUID(), spurs);
            throw e;
        }
    }
}
