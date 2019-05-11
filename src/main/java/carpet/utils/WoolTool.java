package carpet.utils;

import carpet.CarpetSettings;
import carpet.helpers.HopperCounter;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.BlockState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WoolTool
{
    private static final HashMap<MaterialColor,DyeColor> Material2Dye = new HashMap<>();
    static
    {
        for (DyeColor color: DyeColor.values())
        {
            Material2Dye.put(color.getMaterialColor(),color);
        }
    }

    public static void carpetPlacedAction(DyeColor color, PlayerEntity placer, BlockPos pos, World worldIn)
    {
		if (!CarpetSettings.getBool("carpets"))
		{
			return;
		}
        switch (color)
        {
            case PINK:
                if (CarpetSettings.getBool("commandSpawn"))
                    Messenger.send(placer, SpawnReporter.report(pos, worldIn));

                break;
            case BLACK:
                if (CarpetSettings.getBool("commandSpawn"))
                    Messenger.send(placer, SpawnReporter.show_mobcaps(pos, worldIn));
                break;
            case BROWN:
                if (CarpetSettings.getBool("commandDistance"))
                {
                    ServerCommandSource source = placer.getCommandSource();
                    if (!DistanceCalculator.hasStartingPoint(source) || placer.isSneaking()) {
                        DistanceCalculator.setStart(source, new Vec3d(pos));
                    }
                    else {
                        DistanceCalculator.setEnd(source, new Vec3d(pos));
                    }
                }
                break;
            case GRAY:
                if (CarpetSettings.getBool("commandInfo"))
                    Messenger.send(placer, BlockInfo.blockInfo(pos.down(), worldIn));
                break;
            case YELLOW:
                if (CarpetSettings.getBool("commandInfo"))
                    Messenger.m(placer, "r use data get entity command");
                    //EntityInfo.issue_entity_info(placer);
                break;
			case GREEN:
                if (CarpetSettings.getBool("hopperCounters"))
                {
                    DyeColor under = getWoolColorAtPosition(worldIn, pos.down());
                    if (under == null) return;
                    HopperCounter counter = HopperCounter.getCounter(under.toString());
                    if (counter != null)
                        Messenger.send(placer, counter.format(worldIn.getServer(), false, false));
                }
				break;
			case RED:
                if (CarpetSettings.getBool("hopperCounters"))
                {
                    DyeColor under = getWoolColorAtPosition(worldIn, pos.down());
                    if (under == null) return;
                    HopperCounter counter = HopperCounter.getCounter(under.toString());
                    if (counter == null) return;
                    counter.reset(placer.getServer());
                    List<BaseComponent> res = new ArrayList<>();
                    res.add(Messenger.s(String.format("%s counter reset",under.toString())));
                    Messenger.send(placer, res);
                }
			    break;
        }
    }

    public static DyeColor getWoolColorAtPosition(World worldIn, BlockPos pos)
    {
        BlockState state = worldIn.getBlockState(pos);
        if (state.getMaterial() != Material.WOOL || !state.isSimpleFullBlock(worldIn, pos))
            return null;
        return Material2Dye.get(state.getTopMaterialColor(worldIn, pos));
    }
}
