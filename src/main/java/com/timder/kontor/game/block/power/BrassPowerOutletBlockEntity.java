package com.timder.kontor.game.block.power;

import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.power.PowerKind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BrassPowerOutletBlockEntity extends AbstractPowerOutletBlockEntity {

    private int purchasedSu;

    public BrassPowerOutletBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected int getStressUnits() {
        return purchasedSu;
    }

    @Override
    protected PowerKind getKind() {
        return PowerKind.DYNAMIC;
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) {
            return;
        }
        int target = targetStressUnits();
        if (target != purchasedSu) {
            purchasedSu = target;
            updateGeneratedRotation();
        }
    }

    private int targetStressUnits() {
        if (getGeneratedSpeed() == 0 || !hasNetwork()) {
            return 0;
        }

        long target = purchasedSu;
        float surplus = capacity - stress;
        if (surplus < 0) {
            target += (long) Math.ceil(-surplus);
        } else if (surplus >= 1) {
            target -= (long) Math.floor(surplus);
        }
        // TODO: Decide whether minimum should be 0 or 1
        // 0: Network will still be powered and shafts will turn, but with 0 generated SU. Looks kinda cursed
        // 1: Player always has to pay, even if the network does not have any stress
        return (int) Math.max(1L, Math.min(CompanyConfig.BRASS_OUTLET_MAX_SU.get(), target));
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("PurchasedSu", purchasedSu);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        purchasedSu = tag.getInt("PurchasedSu");
    }
}
