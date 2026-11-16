package com.timder.kontor.game.wiki.page;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.game.ui.element.WikiUtils;
import net.minecraft.network.chat.Component;

public class EmployeesWikiPage {
    public static void build(UIElement page) {
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.employees.p1")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.employees.b1")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.employees.p2")));
        page.addChild(WikiUtils.bulletList(Component.translatable("wiki.createkontor.page.employees.conditions")));
        page.addChild(WikiUtils.paragraph(Component.translatable(
                "wiki.createkontor.page.employees.p3", CompanyConfig.HIRE_BONUS_SALARY_MULTIPLE.get())));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.employees.b2")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.employees.p4")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.employees.b3")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.employees.p5")));
    }
}
