package com.timder.kontor.client.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.timder.kontor.game.block.power.PowerOutletBlock;
import java.util.ArrayList;
import java.util.List;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.EntityElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class PowerOutletScenes {
    public static void andesiteOutlet(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("andesite_outlet", "Buying Stress Units");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos outlet = util.grid().at(1, 1, 2);
        BlockPos lever = util.grid().at(1, 1, 1);
        Selection drillContraption = util.select().fromTo(2, 1, 2, 4, 1, 2);
        Selection rotatingParts = util.select().fromTo(1, 1, 2, 3, 1, 2);

        scene.world().showSection(util.select().position(outlet), Direction.DOWN);
        scene.idle(10);
        scene.overlay()
                .showText(90)
                .text(
                        "The Andesite Outlet allows companies to buy stress units. The company it is bound to pays for them.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(outlet))
                .placeNearTarget();
        scene.idle(100);

        scene.world().showSection(drillContraption, Direction.DOWN);
        scene.idle(20);

        Vec3 suBox = util.vector().blockSurface(outlet, Direction.NORTH);
        scene.overlay().showFilterSlotInput(suBox, Direction.NORTH, 80);
        scene.overlay().showControls(suBox, Pointing.DOWN, 80).rightClick();
        scene.idle(10);
        scene.overlay()
                .showText(70)
                .text("The amount of stress units is set on the sides of the outlet.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(suBox);
        scene.idle(80);

        Vec3 rpmBox = util.vector().blockSurface(outlet, Direction.WEST);
        scene.overlay().showFilterSlotInput(rpmBox, Direction.WEST, 80);
        scene.overlay().showControls(rpmBox, Pointing.DOWN, 80).rightClick();
        scene.idle(10);
        scene.overlay()
                .showText(70)
                .text("Speed and direction can be set on the back of the outlet.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(rpmBox);
        scene.idle(80);

        scene.world().showSection(util.select().position(lever), Direction.DOWN);
        scene.idle(10);
        scene.overlay()
                .showText(70)
                .text("It only provides stress units while it is powered with redstone.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(lever))
                .placeNearTarget();
        scene.idle(80);

        scene.world().toggleRedstonePower(util.select().position(lever));
        scene.effects().indicateRedstone(lever);
        scene.world().modifyBlock(outlet, state -> state.setValue(PowerOutletBlock.POWERED, true), false);
        scene.idle(5);
        scene.world().setKineticSpeed(rotatingParts, 16);
        scene.effects().rotationSpeedIndicator(outlet);
        breakBlock(scene, util, util.grid().at(4, 1, 2));
        scene.idle(30);

        scene.overlay()
                .showText(90)
                .text(
                        "The company is billed for every provided stress unit, whether they are used or not. Engineers Googles show the daily cost.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(outlet))
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(70).text("If you need more information about outlets, ask a consultant.");
    }

    public static void brassOutlet(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("brass_outlet", "Dynamically Buying Stress Units");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(10);

        BlockPos outlet = util.grid().at(1, 1, 2);
        BlockPos lever = util.grid().at(1, 1, 1);
        Selection lowerContraption = util.select().fromTo(2, 1, 2, 4, 1, 2);
        Selection lowerRotatingParts = util.select().fromTo(1, 1, 2, 3, 1, 2);
        Selection upperContraption = util.select().fromTo(2, 2, 2, 4, 3, 2);
        Selection upperRotatingParts = util.select().fromTo(2, 2, 2, 3, 3, 2);

        scene.world().showSection(util.select().position(outlet), Direction.DOWN);
        scene.idle(10);
        scene.overlay()
                .showText(90)
                .text("The Brass Outlet buys exactly as many stress units as the network it is connected to needs.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(outlet))
                .placeNearTarget();
        scene.idle(100);

        scene.world().showSection(lowerContraption, Direction.DOWN);
        scene.idle(20);

        Vec3 rpmBox = util.vector().blockSurface(outlet, Direction.WEST);
        scene.overlay().showFilterSlotInput(rpmBox, Direction.WEST, 80);
        scene.overlay().showControls(rpmBox, Pointing.DOWN, 80).rightClick();
        scene.idle(10);
        scene.overlay()
                .showText(70)
                .text(
                        "Only speed and direction can be set on the back of the outlet. The amount of stress units is chosen automatically.")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(rpmBox);
        scene.idle(80);

        scene.world().showSection(util.select().position(lever), Direction.DOWN);
        scene.idle(10);
        scene.overlay()
                .showText(70)
                .text("Like the Andesite Outlet, it only works while it is powered with redstone.")
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(lever))
                .placeNearTarget();
        scene.idle(80);

        scene.world().toggleRedstonePower(util.select().position(lever));
        scene.effects().indicateRedstone(lever);
        scene.world().modifyBlock(outlet, state -> state.setValue(PowerOutletBlock.POWERED, true), false);
        scene.idle(5);
        scene.world().setKineticSpeed(lowerRotatingParts, 16);
        scene.effects().rotationSpeedIndicator(outlet);
        scene.overlay()
                .showText(90)
                .text("It only buys the stress units the connected network needs.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(util.grid().at(2, 1, 2)))
                .placeNearTarget();
        breakBlock(scene, util, util.grid().at(4, 1, 2));
        scene.idle(10);

        scene.world().showSection(upperContraption, Direction.DOWN);
        scene.idle(10);
        scene.world().setKineticSpeed(upperRotatingParts, 16);
        scene.overlay()
                .showText(90)
                .text("If more machines are connected, the outlet buys more stress units.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(util.grid().at(2, 3, 2)))
                .placeNearTarget();
        breakBlock(scene, util, util.grid().at(4, 2, 2), util.grid().at(4, 3, 2));
        scene.idle(10);

        scene.world().hideSection(upperContraption, Direction.UP);
        scene.idle(10);
        scene.overlay()
                .showText(80)
                .text("If fewer stress units are needed, the outlet buys less.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(util.grid().at(2, 1, 2)))
                .placeNearTarget();
        scene.idle(90);

        scene.overlay()
                .showText(90)
                .text(
                        "The company is only billed for the stress units that were bought. They cost more than the ones from an Andesite Outlet. Engineers Goggles show the daily cost.")
                .attachKeyFrame()
                .pointAt(util.vector().topOf(outlet))
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(70).text("If you need more information about outlets, ask a consultant.");
    }

    private static void breakBlock(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos... positions) {
        for (int i = 0; i < 10; i++) {
            scene.idle(10);
            for (BlockPos pos : positions) {
                scene.world().incrementBlockBreakingProgress(pos);
            }
        }
        List<ElementLink<EntityElement>> drops = new ArrayList<>();
        for (BlockPos pos : positions) {
            scene.world().hideSection(util.select().position(pos), Direction.UP);
            drops.add(scene.world()
                    .createItemEntity(
                            util.vector().centerOf(pos), util.vector().of(0, .1f, 0), new ItemStack(Items.STONE)));
        }
        scene.idle(20);
        for (ElementLink<EntityElement> drop : drops) {
            scene.world().modifyEntity(drop, Entity::discard);
        }
    }
}
