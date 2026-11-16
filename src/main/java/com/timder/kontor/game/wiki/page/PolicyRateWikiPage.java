package com.timder.kontor.game.wiki.page;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.config.EconomyConfig;
import com.timder.kontor.game.ui.element.WikiUtils;
import net.minecraft.network.chat.Component;

public class PolicyRateWikiPage {
    public static void build(UIElement page) {
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.policy_rate.p1")));
        page.addChild(WikiUtils.paragraph(Component.translatable(
                "wiki.createkontor.page.policy_rate.p2", EconomyConfig.POLICY_DECISION_INTERVAL_DAYS.get())));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.policy_rate.p3")));
    }
}
