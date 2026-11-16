package com.timder.kontor.registry;

import static com.timder.kontor.registry.KontorRegistries.REGISTRATE;

import com.timder.kontor.client.PowerOutletRenderer;
import com.timder.kontor.game.block.*;
import com.timder.kontor.game.block.buyer.BuyerDeskBlockEntity;
import com.timder.kontor.game.block.employee.EmployeeDeskBlockEntity;
import com.timder.kontor.game.block.power.AndesitePowerOutletBlockEntity;
import com.timder.kontor.game.block.power.BrassPowerOutletBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public final class KontorBlockEntities {

    public static final BlockEntityEntry<ShippingExitBlockEntity> SHIPPING_EXIT = REGISTRATE
            .blockEntity("shipping_exit", ShippingExitBlockEntity::new)
            .validBlocks(KontorBlocks.SHIPPING_EXIT)
            .register();

    public static final BlockEntityEntry<PaymentTerminalBlockEntity> PAYMENT_TERMINAL = REGISTRATE
            .blockEntity("payment_terminal", PaymentTerminalBlockEntity::new)
            .validBlocks(KontorBlocks.PAYMENT_TERMINAL)
            .register();

    public static final BlockEntityEntry<AndesitePowerOutletBlockEntity> ANDESITE_POWER_OUTLET = REGISTRATE
            .blockEntity("andesite_power_outlet", AndesitePowerOutletBlockEntity::new)
            .validBlocks(KontorBlocks.ANDESITE_OUTLET)
            .renderer(() -> PowerOutletRenderer::new)
            .register();

    public static final BlockEntityEntry<BrassPowerOutletBlockEntity> BRASS_POWER_OUTLET = REGISTRATE
            .blockEntity("brass_power_outlet", BrassPowerOutletBlockEntity::new)
            .validBlocks(KontorBlocks.BRASS_OUTLET)
            .renderer(() -> PowerOutletRenderer::new)
            .register();

    public static final BlockEntityEntry<KontorDeskBlockEntity> KONTOR_DESK = REGISTRATE
            .blockEntity("kontor_desk", KontorDeskBlockEntity::new)
            .validBlocks(KontorBlocks.KONTOR_DESK)
            .register();

    public static final BlockEntityEntry<EmployeeDeskBlockEntity> EMPLOYEE_DESK = REGISTRATE
            .blockEntity("employee_desk", EmployeeDeskBlockEntity::new)
            .validBlocks(
                    KontorBlocks.LAWYER_DESK,
                    KontorBlocks.MARKET_ANALYST_DESK,
                    KontorBlocks.CONSULTANT_DESK,
                    KontorBlocks.FINANCES_MANAGER_DESK,
                    KontorBlocks.PROPERTY_MANAGER_DESK)
            .register();

    public static final BlockEntityEntry<BuyerDeskBlockEntity> BUYER_DESK = REGISTRATE
            .blockEntity("buyer_desk", BuyerDeskBlockEntity::new)
            .validBlocks(KontorBlocks.BUYER_DESK)
            .register();

    public static final BlockEntityEntry<LogisticsManagerDeskBlockEntity> LOGISTICS_MANAGER_DESK = REGISTRATE
            .blockEntity("logistics_manager_desk", LogisticsManagerDeskBlockEntity::new)
            .validBlocks(KontorBlocks.LOGISTICS_MANAGER_DESK)
            .register();

    static void touch() {}

    private KontorBlockEntities() {}
}
