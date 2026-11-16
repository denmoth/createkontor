package com.timder.kontor.game.wiki.page;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.config.EconomyConfig;
import com.timder.kontor.game.ui.element.WikiUtils;
import net.minecraft.network.chat.Component;

public class EconomicCycleWikiPage {
    public static void build(UIElement page) {
        page.addChild(WikiUtils.paragraph(Component.translatable(
                "wiki.createkontor.page.economic_cycle.p1",
                EconomyConfig.MIN_CYCLE_LENGTH.get(),
                EconomyConfig.MAX_CYCLE_LENGTH.get())));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.economic_cycle.b1")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.economic_cycle.p2")));
        page.addChild(WikiUtils.bulletList(Component.translatable("wiki.createkontor.page.economic_cycle.phases")));

        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.economic_cycle.p3")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.economic_cycle.p4")));
    }
}
