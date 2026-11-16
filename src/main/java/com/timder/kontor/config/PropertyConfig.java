package com.timder.kontor.config;

import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.property.*;
import com.timder.kontor.core.company.property.measurement.MeasurementGrid;
import com.timder.kontor.game.property.BiomeFrequencySavedData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class PropertyConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue MEASUREMENT_CENTER_X;
    public static final ModConfigSpec.IntValue MEASUREMENT_CENTER_Z;
    public static final ModConfigSpec.IntValue MEASUREMENT_RADIUS_BLOCKS;
    public static final ModConfigSpec.IntValue MEASUREMENT_STEP_BLOCKS;
    public static final ModConfigSpec.IntValue MEASUREMENT_SAMPLES_PER_TICK;
    public static final ModConfigSpec.IntValue MEASUREMENT_CEILING_SAMPLE_Y;
    public static final ModConfigSpec.IntValue MEASUREMENT_MAX_MILLIS_PER_TICK;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MEASUREMENT_AUTO_DIMENSIONS;
    public static final ModConfigSpec.DoubleValue PRICING_RARITY_EXPONENT;
    public static final ModConfigSpec.DoubleValue PRICING_MIN_FACTOR;
    public static final ModConfigSpec.DoubleValue PRICING_MAX_FACTOR;
    public static final ModConfigSpec.DoubleValue DAILY_RENT_RATE;
    public static final ModConfigSpec.DoubleValue RESALE_SHARE;
    public static final ModConfigSpec.IntValue MANAGER_RADIUS;
    public static final ModConfigSpec.IntValue MAX_LOADED_CHUNKS;

    private record DimensionSetting(
            String dimension,
            ModConfigSpec.ConfigValue<String> referenceBiome,
            ModConfigSpec.IntValue referencePrice) {}

    private static final List<DimensionSetting> DIMENSIONS = new ArrayList<>();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("How the biome frequencies of a dimension are measured.")
                .push("measurement");

        MEASUREMENT_CENTER_X = builder.comment("X coordinate of the middle of the measured area, in every dimension")
                .defineInRange("centerX", 0, -29_999_984, 29_999_984);
        MEASUREMENT_CENTER_Z = builder.comment("Z coordinate of the middle of the measured area, in every dimension")
                .defineInRange("centerZ", 0, -29_999_984, 29_999_984);
        MEASUREMENT_RADIUS_BLOCKS = builder.comment("How far the measured area reaches from its middle")
                .defineInRange("radiusBlocks", 16_384, 0, 1_000_000);
        MEASUREMENT_STEP_BLOCKS =
                builder.comment("Distance between two sample points").defineInRange("stepBlocks", 128, 1, 100_000);
        MEASUREMENT_SAMPLES_PER_TICK = builder.comment(
                        "How many points are sampled per server tick while a measurement runs")
                .defineInRange("samplesPerTick", 64, 1, 100_000);
        MEASUREMENT_MAX_MILLIS_PER_TICK = builder.comment("Max time spent on sampling per server tick in ms.")
                .defineInRange("maxMillisPerTick", 40, 1, 1_000);
        MEASUREMENT_CEILING_SAMPLE_Y = builder.comment("Height at which dimensions with a ceiling are sampled")
                .defineInRange("ceilingSampleY", 64, -64, 320);
        MEASUREMENT_AUTO_DIMENSIONS = builder.comment(
                        "Dimensions that are measured automatically when the server has started")
                .defineListAllowEmpty(
                        "autoDimensions",
                        List.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"),
                        () -> "minecraft:overworld",
                        value -> value instanceof String string && ResourceLocation.tryParse(string) != null);

        builder.pop();

        builder.comment("Land pricing").push("pricing");
        PRICING_RARITY_EXPONENT = builder.comment("Rarity factor. 1 = prices follow rarity linearly")
                .defineInRange("rarityExponent", 0.4, 0.0, 5.0);
        PRICING_MIN_FACTOR = builder.comment("Min Price Factor").defineInRange("minFactor", 0.2, 0.01, 1.0);
        PRICING_MAX_FACTOR = builder.comment("Max Price Factor. Also applied for biomes that were never hit")
                .defineInRange("maxFactor", 50.0, 1.0, 1_000.0);
        defineDimension(builder, "overworld", "minecraft:overworld", "minecraft:plains", 8000);
        defineDimension(builder, "nether", "minecraft:the_nether", "minecraft:nether_wastes", 8000);
        defineDimension(builder, "end", "minecraft:the_end", "minecraft:end_highlands", 16000);
        builder.pop();

        builder.comment("Owning and renting land").push("ownership");
        DAILY_RENT_RATE = builder.comment("Daily rent of a rented chunk as a share of its current price")
                .defineInRange("dailyRentRate", 0.02, 0.0, 1.0);
        RESALE_SHARE = builder.comment("Share of the current price a company gets when it sells a chunk")
                .defineInRange("resaleShare", 0.6, 0.0, 1.0);
        MANAGER_RADIUS = builder.comment("The radius in chunks that a property manager can manage")
                .defineInRange("managerRadius", 8, 1, 32);
        MAX_LOADED_CHUNKS = builder.comment("How many of its held chunks a company can keep loaded")
                .defineInRange("maxLoadedChunks", 32, 0, 1_000);
        builder.pop();

        SPEC = builder.build();
    }

    private static void defineDimension(
            ModConfigSpec.Builder builder, String name, String dimension, String biome, int priceInDollars) {
        builder.comment("Reference biome and price of " + dimension).push(name);
        ModConfigSpec.ConfigValue<String> referenceBiome =
                builder.define("referenceBiome", biome, value -> value instanceof String text && !text.isBlank());
        ModConfigSpec.IntValue referencePrice = builder.comment("Price of a chunk of the reference biome")
                .defineInRange("referencePriceInDollars", priceInDollars, 1, Integer.MAX_VALUE);
        builder.pop();
        DIMENSIONS.add(new DimensionSetting(dimension, referenceBiome, referencePrice));
    }

    public static PropertyParams toPropertyParams() {
        List<DimensionPricing> dimensions = new ArrayList<>();
        for (DimensionSetting setting : DIMENSIONS) {
            dimensions.add(new DimensionPricing(
                    new DimensionId(setting.dimension()),
                    new BiomeId(setting.referenceBiome().get()),
                    Money.ofDollars(setting.referencePrice().get())));
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        Map<DimensionId, BiomeFrequencies> frequencies =
                server == null ? Map.of() : BiomeFrequencySavedData.get(server).frequencies();
        return new PropertyParams(
                new PropertyPricingParams(
                        PRICING_RARITY_EXPONENT.get(), PRICING_MIN_FACTOR.get(), PRICING_MAX_FACTOR.get(), dimensions),
                frequencies,
                DAILY_RENT_RATE.get(),
                RESALE_SHARE.get(),
                MAX_LOADED_CHUNKS.get());
    }

    public static MeasurementGrid toMeasurementGrid() {
        return new MeasurementGrid(
                MEASUREMENT_CENTER_X.get(),
                MEASUREMENT_CENTER_Z.get(),
                MEASUREMENT_RADIUS_BLOCKS.get(),
                MEASUREMENT_STEP_BLOCKS.get());
    }
}
