package com.timder.kontor.game.villager;

import com.timder.kontor.game.CompanySavedData;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Items;

public class FollowEmeraldBehaviour extends Behavior<Villager> {

    private static final float SPEED_MODIFIER = 0.6F;

    private static final int RANGE = 11;
    private static final int MAX_FOLLOWERS = 4;
    private static final int CLOSE_ENOUGH_BLOCKS = 3;
    private static final double STOP_DISTANCE_SQR = 6.25;
    private static final int CALM_DOWN_TICKS = 100;
    private static final int START_CHECK_INTERVAL_TICKS = 3;

    private static final int MAX_RUN_TICKS = 24_000;

    @Nullable
    private ServerPlayer leader;

    private long calmDownUntil;

    public FollowEmeraldBehaviour() {
        super(Map.of(), MAX_RUN_TICKS, MAX_RUN_TICKS);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Villager villager) {
        long now = level.getGameTime();
        if (now < calmDownUntil || (now + villager.getId()) % START_CHECK_INTERVAL_TICKS != 0) {
            return false;
        }
        if (!canFollow(villager)) {
            return false;
        }
        ServerPlayer found = findLeader(level, villager);
        if (found == null || !isAmongNearestFollowers(level, villager, found)) {
            return false;
        }
        leader = found;
        return true;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Villager villager, long gameTime) {
        if (!canFollow(villager)) {
            return false;
        }
        leader = findLeader(level, villager);
        return leader != null;
    }

    @Override
    protected void tick(ServerLevel level, Villager villager, long gameTime) {
        if (leader == null) {
            return;
        }
        Brain<Villager> brain = villager.getBrain();
        brain.setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(leader, true));
        if (villager.distanceToSqr(leader) < STOP_DISTANCE_SQR) {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            villager.getNavigation().stop();
        } else {
            brain.setMemory(
                    MemoryModuleType.WALK_TARGET,
                    new WalkTarget(new EntityTracker(leader, false), SPEED_MODIFIER, CLOSE_ENOUGH_BLOCKS));
        }
    }

    @Override
    protected void stop(ServerLevel level, Villager villager, long gameTime) {
        leader = null;
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        villager.getNavigation().stop();
        calmDownUntil = gameTime + CALM_DOWN_TICKS;
    }

    private static boolean canFollow(Villager villager) {
        if (!villager.isAlive()
                || villager.isBaby()
                || villager.isPassenger()
                || villager.isSleeping()
                || villager.isTrading()
                || villager.isLeashed()) {
            return false;
        }
        Brain<Villager> brain = villager.getBrain();
        return !brain.isActive(Activity.PANIC)
                && !brain.isActive(Activity.HIDE)
                && !brain.isActive(Activity.RAID)
                && !brain.isActive(Activity.PRE_RAID);
    }

    @Nullable
    private static ServerPlayer findLeader(ServerLevel level, Villager villager) {
        double bestDistanceSqr = RANGE * RANGE;
        ServerPlayer best = null;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()
                    || player.isShiftKeyDown()
                    || player.getMainHandItem().is(Items.EMERALD)
                    || player.getOffhandItem().is(Items.EMERALD)) {
                continue;
            }
            double distanceSqr = villager.distanceToSqr(player);
            if (distanceSqr > bestDistanceSqr || !isCompanyMember(level.getServer(), player)) {
                continue;
            }
            best = player;
            bestDistanceSqr = distanceSqr;
        }
        return best;
    }

    private static boolean isCompanyMember(MinecraftServer server, ServerPlayer player) {
        return CompanySavedData.get(server)
                .getRegistry()
                .companyOf(player.getUUID())
                .isPresent();
    }

    private static boolean isAmongNearestFollowers(ServerLevel level, Villager villager, ServerPlayer player) {
        double ownDistanceSqr = villager.distanceToSqr(player);
        int closer = 0;
        for (Villager other : level.getEntitiesOfClass(
                Villager.class,
                player.getBoundingBox().inflate(RANGE),
                candidate -> candidate != villager && canFollow(candidate))) {
            if (other.distanceToSqr(player) < ownDistanceSqr && ++closer >= MAX_FOLLOWERS) {
                return false;
            }
        }
        return true;
    }
}
