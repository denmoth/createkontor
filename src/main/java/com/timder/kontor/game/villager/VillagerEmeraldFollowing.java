package com.timder.kontor.game.villager;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class VillagerEmeraldFollowing {

    private static final int CHECK_INTERVAL_TICKS = 40;
    private static final Set<Brain<?>> ATTACHED =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Villager villager)
                || villager.level().isClientSide()
                || villager.tickCount % CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        Brain<Villager> brain = villager.getBrain();
        if (ATTACHED.add(brain)) {
            brain.addActivity(Activity.CORE, 2, ImmutableList.of(new FollowEmeraldBehaviour()));
        }
    }
}
