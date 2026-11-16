package com.timder.kontor.game.block.power;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.motor.KineticScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.timder.kontor.CreateKontor;
import com.timder.kontor.config.CompanyConfig;
import com.timder.kontor.core.company.Company;
import com.timder.kontor.core.company.CompanyId;
import com.timder.kontor.core.company.CompanyRegistry;
import com.timder.kontor.core.company.financial.Money;
import com.timder.kontor.core.company.power.PowerKind;
import com.timder.kontor.core.company.power.PowerParams;
import com.timder.kontor.core.company.power.PowerRules;
import com.timder.kontor.core.economy.Economy;
import com.timder.kontor.game.CompanySavedData;
import com.timder.kontor.game.block.company.CompanyBlockSupport;
import com.timder.kontor.game.block.company.CompanyBound;
import com.timder.kontor.util.ComponentFormatting;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPowerOutletBlockEntity extends GeneratingKineticBlockEntity implements CompanyBound {

    private static final int FLUSH_INTERVAL = 20;

    @Nullable
    protected CompanyId companyId;

    protected String companyName = "";
    protected long pendingSuTicks;

    private ScrollValueBehaviour rotationSpeed;
    private boolean inactive;

    public AbstractPowerOutletBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract int getStressUnits();

    protected abstract PowerKind getKind();

    @Nullable
    @Override
    public CompanyId getCompanyId() {
        return companyId;
    }

    @Override
    public void setCompanyId(@Nullable CompanyId id) {
        if (Objects.equals(companyId, id)) {
            return;
        }
        flushPower();
        updateBoundResourceCount(companyId, id);
        companyId = id;
        companyName = resolveCompanyName();
        CompanyBlockSupport.afterCompanyIdChanged(this);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        int maxRpm = CompanyConfig.SPEC.isLoaded() ? CompanyConfig.OUTLET_MAX_RPM.get() : 256;
        rotationSpeed = new KineticScrollValueBehaviour(
                Component.translatable("ui.createkontor.power_outlet.rotation_speed"),
                this,
                new CenteredSideValueBoxTransform((state, direction) ->
                        direction == state.getValue(PowerOutletBlock.FACING).getOpposite()));
        rotationSpeed.between(-maxRpm, maxRpm);
        rotationSpeed.value = Math.min(16, maxRpm);
        rotationSpeed.withCallback(value -> updateGeneratedRotation());
        behaviours.add(rotationSpeed);
    }

    protected void flushPower() {
        if (pendingSuTicks == 0 || companyId == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        CompanySavedData data = CompanySavedData.get(serverLevel.getServer());
        Company company = data.getRegistry().get(companyId).orElse(null);
        if (company != null) {
            PowerRules.add(company, getKind(), pendingSuTicks);
            data.setDirty();
        }
        pendingSuTicks = 0;
    }

    private void updateInactive() {
        if (companyId == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Company company = CompanySavedData.get(serverLevel.getServer())
                .getRegistry()
                .get(companyId)
                .orElse(null);
        boolean shouldBeInactive = company == null || !company.isOperational();
        if (shouldBeInactive == inactive) {
            return;
        }
        inactive = shouldBeInactive;
        updateGeneratedRotation();
        notifyUpdate();
    }

    @Override
    public float getGeneratedSpeed() {
        BlockState state = getBlockState();
        if (rotationSpeed == null
                || !(state.getBlock() instanceof PowerOutletBlock)
                || !state.getValue(PowerOutletBlock.POWERED)
                || inactive
                || getCompanyId() == null) {
            return 0;
        }
        return convertToDirection(rotationSpeed.getValue(), state.getValue(PowerOutletBlock.FACING));
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = 0;
        if (getGeneratedSpeed() != 0 && rotationSpeed != null) {
            capacity = capacityPerRpm(getStressUnits(), Math.abs(rotationSpeed.getValue()));
        }
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public void onChunkUnloaded() {
        flushPower();
        super.onChunkUnloaded();
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        CompanyBlockSupport.writeCompanyId(companyId, tag);
        CompanyBlockSupport.writeCompanyName(companyName, tag);
        if (clientPacket) {
            tag.putBoolean("Inactive", inactive);
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        companyId = CompanyBlockSupport.readCompanyId(tag);
        companyName = CompanyBlockSupport.readCompanyName(tag);
        if (clientPacket) {
            inactive = tag.getBoolean("Inactive");
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!hasSource() || Math.abs(getGeneratedSpeed()) > Math.abs(getTheoreticalSpeed())) {
            updateGeneratedRotation();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide() || companyId == null) {
            return;
        }
        if (getGeneratedSpeed() != 0) {
            pendingSuTicks += getStressUnits();
        }
        if ((level.getGameTime() % FLUSH_INTERVAL) == 0) {
            flushPower();
            updateInactive();
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level != null && !level.isClientSide()) {
            String resolved = resolveCompanyName();
            if (!resolved.equals(companyName)) {
                companyName = resolved;
                notifyUpdate();
            }
        }
    }

    @Override
    public void remove() {
        super.remove();
        setCompanyId(null);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (getGeneratedSpeed() != 0) {
            tooltip.add(Component.literal(""));
        }

        if (companyId == null || companyName.isEmpty()) {
            CreateLang.builder(CreateKontor.MODID)
                    .add(ComponentFormatting.errorTranslatable(CompanyBlockSupport.GOGGLE_COMPANY_BLOCK_UNBOUND))
                    .forGoggles(tooltip, 0);
            return true;
        }

        CreateLang.builder()
                .add(Component.translatable(CompanyBlockSupport.GOGGLE_COMPANY_BLOCK)
                        .withStyle(ChatFormatting.WHITE))
                .forGoggles(tooltip, 0);
        CreateLang.builder()
                .add(ComponentFormatting.highlightStandard(companyName))
                .forGoggles(tooltip, 1);

        if (inactive) {
            CreateLang.builder(CreateKontor.MODID)
                    .add(ComponentFormatting.errorTranslatable("goggle.createkontor.power_outlet.inactive"))
                    .forGoggles(tooltip, 0);
        }

        CreateLang.builder()
                .add(ComponentFormatting.standardTranslatable("goggle.createkontor.power_outlet.daily_cost"))
                .forGoggles(tooltip, 0);
        Money perDay = getGeneratedSpeed() == 0
                ? Money.ZERO
                : PowerRules.cost(
                        (long) getStressUnits() * Economy.DAY_LENGTH,
                        getKind(),
                        new PowerParams(
                                CompanyConfig.SU_PRICE_PER_SU_DAY_IN_DOLLARS.get(),
                                CompanyConfig.BRASS_OUTLET_PRICE_FACTOR.get()));
        CreateLang.builder(CreateKontor.MODID)
                .add(Component.translatable(
                                "goggle.createkontor.power_outlet.daily_cost.value",
                                ComponentFormatting.moneyNegative(perDay))
                        .withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        return true;
    }

    public static float capacityPerRpm(int stressUnits, int rpm) {
        if (stressUnits <= 0 || rpm <= 0) {
            return 0f;
        }
        float capacity = (float) stressUnits / rpm;
        while (capacity * rpm < stressUnits) {
            capacity = Math.nextUp(capacity);
        }
        return capacity;
    }

    protected void updateBoundResourceCount(@Nullable CompanyId oldId, @Nullable CompanyId newId) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        CompanySavedData data = CompanySavedData.get(serverLevel.getServer());
        CompanyRegistry registry = data.getRegistry();
        boolean changedAny = false;
        if (oldId != null) {
            Company old = registry.get(oldId).orElse(null);
            if (old != null) {
                old.unbindResource(PowerOutletBlock.RESOURCE_KEY);
                changedAny = true;
            }
        }
        if (newId != null) {
            Company next = registry.get(newId).orElse(null);
            if (next != null) {
                next.bindResource(PowerOutletBlock.RESOURCE_KEY);
                changedAny = true;
            }
        }
        if (changedAny) {
            data.setDirty();
        }
    }

    protected String resolveCompanyName() {
        if (companyId == null || !(level instanceof ServerLevel serverLevel)) {
            return "";
        }
        return CompanySavedData.get(serverLevel.getServer())
                .getRegistry()
                .get(companyId)
                .map(Company::name)
                .orElse("");
    }
}
