package com.timder.kontor.game.wiki.page;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.compat.numismatics.NumismaticsConfig;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.game.ui.element.WikiUtils;
import net.minecraft.network.chat.Component;

public class NumismaticsWikiPage {
    public static void build(UIElement page) {
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.numismatics.p1")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.numismatics.b1")));
        page.addChild(WikiUtils.paragraph(Component.translatable(
                "wiki.createkontor.page.numismatics.p2",
                Money.fromDollar(NumismaticsConfig.DOLLARS_PER_SPUR.get()).toString())));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.numismatics.b2")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.numismatics.p3")));
    }
}
