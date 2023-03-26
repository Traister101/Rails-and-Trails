package mod.traister101.rnt.objects.blocks;

import mcp.MethodsReturnNonnullByDefault;
import net.dries007.tfc.api.types.Rock;
import net.dries007.tfc.util.OreDictionaryHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RoadSlab extends BlockSlab {

    public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);
    public final Block modelBlock;
    protected Half halfSlab;

    public RoadSlab(Rock rock) {
        super(Road.get(rock).getDefaultState().getMaterial());

        IBlockState state = blockState.getBaseState();
        if (!isDouble()) state = state.withProperty(HALF, EnumBlockHalf.BOTTOM);
        setDefaultState(state.withProperty(VARIANT, Variant.DEFAULT));
        modelBlock = Road.get(rock);
        useNeighborBrightness = true;
        setLightOpacity(255);
    }

    @Override
    public void onEntityWalk(final World worldIn, final BlockPos pos, final Entity entityIn) {

        final double modifier = 1.2;
        entityIn.motionX *= modifier;
        entityIn.motionZ *= modifier;

        super.onEntityWalk(worldIn, pos, entityIn);
    }

    @Override
    public String getTranslationKey(int meta) {
        return super.getTranslationKey();
    }

    @Override
    public boolean isDouble() {
        return false;
    }

    @Override
    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }

    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return Variant.DEFAULT;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;

        if (!isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP)
            i |= 8;

        return i;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return isDouble() ? new BlockStateContainer(this, VARIANT) : new BlockStateContainer(this, HALF, VARIANT);
    }

    public enum Variant implements IStringSerializable {
        DEFAULT;

        @Override
        public String getName() {
            return "default";
        }
    }

    public static class Double extends RoadSlab {

        private static final Map<Rock, Double> ROCK_TABLE = new HashMap<>();

        public Double(Rock rock) {
            super(rock);

            ROCK_TABLE.put(rock, this);
        }

        public static Double get(Rock rock) {
            return ROCK_TABLE.get(rock);
        }


        @Override
        public boolean isDouble() {
            return true;
        }
    }

    public static class Half extends RoadSlab {

        private static final Map<Rock, Half> ROCK_TABLE = new HashMap<>();

        public final Double doubleSlab;

        public Half(Rock rock) {
            super(rock);

            ROCK_TABLE.put(rock, this);

            doubleSlab = Double.get(rock);
            doubleSlab.halfSlab = this;
            halfSlab = this;
            OreDictionaryHelper.register(this, "slab");
        }

        @Override
        public boolean isDouble() {
            return false;
        }
    }
}