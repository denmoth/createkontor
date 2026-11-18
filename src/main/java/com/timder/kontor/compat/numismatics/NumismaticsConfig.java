package com.timder.kontor.compat.numismatics;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class NumismaticsConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue DOLLARS_PER_SPUR;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Exchanging coins of Create: Numismatics into Create: Kontor money").push("currencyExchange");
        DOLLARS_PER_SPUR = builder.comment("Dollars a company gets for one spur")
                .defineInRange("dollarsPerSpur", 1.0, 0.01, 10_000_000.0);
        builder.pop();

        SPEC = builder.build();
    }
}
