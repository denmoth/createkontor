package com.timder.kontor.game.block.power;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.*;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.power.PowerKind;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AndesitePowerOutletBlockEntity extends AbstractPowerOutletBlockEntity {

    private StressUnitsBehaviour stressUnits;

    public AndesitePowerOutletBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected int getStressUnits() {
        return stressUnits.getValue();
    }

    @Override
    protected PowerKind getKind() {
        return PowerKind.FIXED;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        int maxSu = CompanyConfig.SPEC.isLoaded() ? CompanyConfig.ANDESITE_OUTLET_MAX_SU.get() : 1024;
        stressUnits = new StressUnitsBehaviour(this, maxSu);
        stressUnits.value = Math.min(512, maxSu);
        stressUnits.withCallback(value -> updateGeneratedRotation());
        behaviours.add(stressUnits);
    }

    // Necessary because large Max SU values would make the value bar overflow the screen
    private static class StressUnitsBehaviour extends ScrollValueBehaviour {

        private static final BehaviourType<StressUnitsBehaviour> TYPE = new BehaviourType<>("outlet_stress_units");
        private static final String NBT_KEY = "StressUnits";
        private static final int MAX_COLUMNS = 256;

        private final int step;
        private final int columns;

        StressUnitsBehaviour(SmartBlockEntity be, int maximum) {
            super(
                    Component.translatable("ui.createkontor.power_outlet.stress_units"),
                    be,
                    new CenteredSideValueBoxTransform((state, direction) -> direction.getAxis()
                            != state.getValue(PowerOutletBlock.FACING).getAxis()));
            between(1, maximum);
            step = (maximum + MAX_COLUMNS - 1) / MAX_COLUMNS;
            columns = (maximum + step - 1) / step;
        }

        private int stressUnitsOf(int column) {
            return Mth.clamp(column * step, 1, max);
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            return new ValueSettingsBoard(
                    label,
                    columns,
                    Math.min(32, columns),
                    ImmutableList.of(Component.literal("SU")),
                    new ValueSettingsFormatter(settings -> CreateLang.number(stressUnitsOf(settings.value()))
                            .add(CreateLang.text(" SU"))
                            .component()));
        }

        @Override
        public ValueSettings getValueSettings() {
            return new ValueSettings(0, value >= max ? columns : (value + step / 2) / step);
        }

        @Override
        public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
            if (valueSetting.equals(getValueSettings())) {
                return;
            }
            setValue(stressUnitsOf(valueSetting.value()));
            playFeedbackSound(this);
        }

        @Override
        public void write(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
            CompoundTag own = new CompoundTag();
            super.write(own, registries, clientPacket);
            nbt.put(NBT_KEY, own);
        }

        @Override
        public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
            if (!nbt.contains(NBT_KEY)) {
                return;
            }
            super.read(nbt.getCompound(NBT_KEY), registries, clientPacket);
            value = Mth.clamp(value, 1, max);
        }

        @Override
        public int netId() {
            return 1;
        }

        @Override
        public BehaviourType<?> getType() {
            return TYPE;
        }
    }
}
