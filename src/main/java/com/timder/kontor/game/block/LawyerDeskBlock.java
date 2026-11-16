package com.timder.kontor.game.block;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.employee.RoleId;
import com.timder.kontor.game.block.consultant.ConsultantWiki;
import com.timder.kontor.game.block.employee.AbstractEmployeeDeskBlock;
import com.timder.kontor.game.block.employee.EmployeeDeskContext;
import com.timder.kontor.game.ui.LawyerUi;
import com.timder.kontor.game.ui.element.WikiUtils;
import com.timder.kontor.game.wiki.WikiCategory;
import com.timder.kontor.registry.KontorBlocks;
import net.minecraft.network.chat.Component;

public class LawyerDeskBlock extends AbstractEmployeeDeskBlock {

    public static final RoleId ROLE = new RoleId("lawyer");

    public LawyerDeskBlock(Properties properties) {
        super(properties, ROLE, CompanyConfig.LAWYER_SALARY_IN_DOLLARS);
        ConsultantWiki.register(
                "lawyer",
                WikiCategory.EMPLOYEES,
                Component.translatable("employee_role.createkontor.lawyer"),
                this::buildWikiPage,
                KontorBlocks.LAWYER_DESK.getId());
    }

    @Override
    protected ModularUI createEmployeeUi(EmployeeDeskContext context) {
        return LawyerUi.create(context, new LawyerActions(context.desk()));
    }

    @Override
    public int getTopY() {
        return 22;
    }

    private void buildWikiPage(UIElement page) {
        page.addChild(WikiUtils.employeeScene(defaultBlockState()));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.lawyer.p1")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.lawyer.b1")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.lawyer.p2")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.lawyer.b2")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.lawyer.p3")));

        page.addChild(WikiUtils.heading(Component.translatable("wiki.createkontor.page.lawyer.b3")));
        page.addChild(WikiUtils.paragraph(Component.translatable("wiki.createkontor.page.lawyer.p4")));
        page.addChild(WikiUtils.bulletList(Component.translatable("wiki.createkontor.page.lawyer.licenses")));
    }
}
