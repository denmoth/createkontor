package com.timder.kontor.game.wiki.page;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.utils.data.BlockInfo;
import com.lowdragmc.lowdraglib2.utils.virtuallevel.TrackedDummyWorld;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.game.ui.element.WikiUtils;
import com.timder.kontor.registry.KontorBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class PowerOutletsWikiPage {
    public static void build(UIElement page) {
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.power_outlets.p1")));

        var level = new TrackedDummyWorld();
        level.addBlock(new BlockPos(0, 0, 0), BlockInfo.fromBlockState(KontorBlocks.ANDESITE_OUTLET.getDefaultState()));
        level.addBlock(new BlockPos(2, 0, 0), BlockInfo.fromBlockState(KontorBlocks.BRASS_OUTLET.getDefaultState()));
        page.addChild(WikiUtils.scene(
                level,
                WikiUtils.SCENE_DEFAULT_HEIGHT,
                false,
                Component.translatable("wiki.createkontor.page.power_outlets.scene1")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.power_outlets.b1")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.power_outlets.p2")));
        page.addChild(WikiUtils.paragraph(
                Component.translatable("wiki.createkontor.page.power_outlets.p3", CompanyConfig.OUTLET_MAX_RPM.get())));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.power_outlets.b2")));
        page.addChild(WikiUtils.paragraph(Component.translatable(
                "wiki.createkontor.page.power_outlets.p4", CompanyConfig.ANDESITE_OUTLET_MAX_SU.get())));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.power_outlets.b3")));
        page.addChild(WikiUtils.paragraph(Component.translatable(
                "wiki.createkontor.page.power_outlets.p5",
                CompanyConfig.BRASS_OUTLET_MAX_SU.get(),
                CompanyConfig.BRASS_OUTLET_PRICE_FACTOR.get())));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.power_outlets.b4")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.power_outlets.p6")));
    }
}
