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
        NOT_ENOUGH_COINS,
        NOT_OWNER,
        NOT_ENOUGH_PAID_IN,
        NOT_OPERATIONAL,
        NOT_ENOUGH_MONEY,
        ACCOUNT_FULL
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

    public static Result exchangeSpurs(ServerPlayer player, int spurs) {
        if (!isAvailable()) return Result.of(Status.NOT_AVAILABLE);

        MinecraftServer server = player.getServer();
        if (server == null) return Result.of(Status.NOT_AVAILABLE);
        CompanySavedData companyData = CompanySavedData.get(server);
        Company company = companyData.getRegistry().companyOf(player.getUUID()).orElse(null);
        if (company == null) return Result.of(Status.NO_COMPANY);

        long centsPerSpur =
                Money.fromDollar(NumismaticsConfig.DOLLARS_PER_SPUR.get()).cents();
        if (spurs <= 0) return Result.of(Status.INVALID_AMOUNT);
        if (!NumismaticsExchangeRules.moneyFor(spurs, centsPerSpur).isPositive()) return Result.of(Status.TOO_SMALL);

        if (!NumismaticsAccess.take(player.getUUID(), spurs)) return Result.of(Status.NOT_ENOUGH_COINS);

        try {
            long day = EconomySavedData.get(server).getEconomy().currentDay();
            Money credited = NumismaticsExchangeRules.exchange(company, spurs, day, spurs + " spurs", centsPerSpur);
            company.addExchangedSpurs(spurs);
            companyData.setDirty();
            return new Result(Status.OK, credited);
        } catch (RuntimeException e) {
            NumismaticsAccess.give(player.getUUID(), spurs);
            throw e;
        }
    }

    public static Result getSpursBack(ServerPlayer player, int spurs) {
        if (!isAvailable()) return Result.of(Status.NOT_AVAILABLE);

        MinecraftServer server = player.getServer();
        if (server == null) return Result.of(Status.NOT_AVAILABLE);
        CompanySavedData companyData = CompanySavedData.get(server);
        Company company = companyData.getRegistry().companyOf(player.getUUID()).orElse(null);
        if (company == null) return Result.of(Status.NO_COMPANY);
        if (!company.isOwner(player.getUUID())) return Result.of(Status.NOT_OWNER);
        if (spurs <= 0) return Result.of(Status.INVALID_AMOUNT);

        if (spurs > company.exchangedSpurs()) return Result.of(Status.NOT_ENOUGH_PAID_IN);

        long centsPerSpur =
                Money.fromDollar(NumismaticsConfig.DOLLARS_PER_SPUR.get()).cents();
        Money cost = NumismaticsExchangeRules.moneyFor(spurs, centsPerSpur);
        if (!company.isOperational()) return Result.of(Status.NOT_OPERATIONAL);
        if (!NumismaticsExchangeRules.canBuyBack(company, cost)) return Result.of(Status.NOT_ENOUGH_MONEY);

        if (!NumismaticsAccess.give(player.getUUID(), spurs)) return Result.of(Status.ACCOUNT_FULL);

        try {
            long day = EconomySavedData.get(server).getEconomy().currentDay();
            NumismaticsExchangeRules.buyBack(company, day, spurs + " spurs", cost);
            company.removeExchangedSpurs(spurs);
            companyData.setDirty();
            return new Result(Status.OK, cost);
        } catch (RuntimeException e) {
            NumismaticsAccess.take(player.getUUID(), spurs);
            throw e;
        }
    }
}
