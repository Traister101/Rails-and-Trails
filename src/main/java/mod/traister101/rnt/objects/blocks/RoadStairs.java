package mod.traister101.rnt.objects.blocks;

import net.dries007.tfc.api.types.Rock;
import net.dries007.tfc.util.OreDictionaryHelper;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RoadStairs extends BlockStairs {

    public RoadStairs(Rock rock) {
        super(Road.get(rock).getDefaultState());

        final Road road = Road.get(rock);
        final IBlockState state = road.getDefaultState();

        //noinspection ConstantConditions
        setHarvestLevel(road.getHarvestTool(state), road.getHarvestLevel(state));
        OreDictionaryHelper.register(this, "stair");
    }
}