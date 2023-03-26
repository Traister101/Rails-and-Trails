package mod.traister101.rnt.objects.blocks;

import net.dries007.tfc.api.capability.size.IItemSize;
import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.dries007.tfc.api.types.Rock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault

public class Road extends Block implements IItemSize {

    private static final Map<Rock, Road> ROAD_MAP = new HashMap<>();

    public Road(Rock rock) {
        super(Material.ROCK);

        ROAD_MAP.put(rock, this);

        setSoundType(SoundType.STONE);
        setHardness(rock.getRockCategory().getHardness()).setResistance(rock.getRockCategory().getResistance());
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    public void onEntityWalk(final World worldIn, final BlockPos pos, final Entity entityIn) {

        final double modifier = 1.2;
        entityIn.motionX *= modifier;
        entityIn.motionZ *= modifier;

        super.onEntityWalk(worldIn, pos, entityIn);
    }

    public static Road get(Rock rock) {
        return ROAD_MAP.get(rock);
    }

    @Nonnull
    @Override
    public Size getSize(@Nonnull ItemStack itemStack) {
        return Size.SMALL;
    }

    @Nonnull
    @Override
    public Weight getWeight(@Nonnull ItemStack itemStack) {
        return Weight.LIGHT;
    }
}