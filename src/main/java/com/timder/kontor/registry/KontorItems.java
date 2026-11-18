package com.timder.kontor.registry;

import static com.timder.kontor.registry.KontorRegistries.REGISTRATE;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.timder.kontor.compat.numismatics.NumismaticsExchange;
import com.timder.kontor.game.item.CheckbookItem;
import com.timder.kontor.game.item.NotebookItem;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import java.util.List;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class KontorItems {

    public static final List<String> NUMISMATICS_COINS = List.of("spur", "bevel", "sprocket", "cog", "crown", "sun");

    public static final ItemEntry<NotebookItem> NOTEBOOK = REGISTRATE
            .item("notebook", NotebookItem::new)
            .properties(properties -> properties.stacksTo(1))
            .recipe((ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern("NL")
                    .pattern("NP")
                    .define('N', Items.IRON_NUGGET)
                    .define('L', Items.LEATHER)
                    .define('P', Items.PAPER)
                    .unlockedBy("has_kontor_desk", RegistrateRecipeProvider.has(KontorBlocks.KONTOR_DESK.get()))
                    .save(prov))
            .lang("Notebook")
            .onRegisterAfter(Registries.ITEM, KontorItems::tooltip)
            .register();

    public static final ItemEntry<CheckbookItem> CHECKBOOK = REGISTRATE
            .item("checkbook", CheckbookItem::new)
            .properties(properties -> properties.stacksTo(1))
            .recipe((ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern("GGG")
                    .pattern("PPI")
                    .define('G', Items.GOLD_NUGGET)
                    .define('P', Items.PAPER)
                    .define('I', Items.INK_SAC)
                    .unlockedBy("has_kontor_desk", RegistrateRecipeProvider.has(KontorBlocks.KONTOR_DESK.get()))
                    .save(prov))
            .lang("Checkbook")
            .onRegisterAfter(Registries.ITEM, KontorItems::tooltip)
            .register();

    public static void tooltip(Item item) {
        TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE));
    }

    public static void registerVanillaTooltips() {
        if (TooltipModifier.REGISTRY.get(Items.EMERALD) == null) {
            tooltip(Items.EMERALD);
        }
        registerNumismaticsTooltips();
    }

    private static void registerNumismaticsTooltips() {
        if (!NumismaticsExchange.isAvailable()) {
            return;
        }
        for (String coin : NUMISMATICS_COINS) {
            BuiltInRegistries.ITEM
                    .getOptional(ResourceLocation.fromNamespaceAndPath("numismatics", coin))
                    .filter(item -> TooltipModifier.REGISTRY.get(item) == null)
                    .ifPresent(KontorItems::tooltip);
        }
    }

    static void touch() {}

    private KontorItems() {}
}
