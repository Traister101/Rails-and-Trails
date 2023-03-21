package mod.traister101.rnt.objects.blocks;

import net.dries007.tfc.api.capability.size.IItemSize;
import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault

public class Road extends Block implements IItemSize {

    public Road() {
        super(Material.ROCK);
    }

    @Override
    public void onEntityWalk(final World worldIn, final BlockPos pos, final Entity entityIn) {

        final double modifier = 1.2;
        entityIn.motionX *= modifier;
        entityIn.motionZ *= modifier;

        super.onEntityWalk(worldIn, pos, entityIn);
    }

    @Nonnull
    @Override
    public Size getSize(@Nonnull ItemStack itemStack) {
        return Size.NORMAL;
    }

    @Nonnull
    @Override
    public Weight getWeight(@Nonnull ItemStack itemStack) {
        return Weight.MEDIUM;
    }
}