package mod.traister101.rnt.objects.blocks;

import mcp.MethodsReturnNonnullByDefault;
import net.dries007.tfc.api.types.Rock;
import net.dries007.tfc.util.OreDictionaryHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class RoadSlab extends BlockSlab {

	public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);
	public final Block modelBlock;
	protected Half halfSlab;

	public RoadSlab(Rock rock) {
		super(Road.get(rock).getDefaultState().getMaterial());

		IBlockState state = blockState.getBaseState();
		if (!isDouble()) state = state.withProperty(HALF, EnumBlockHalf.BOTTOM);
		setDefaultState(state.withProperty(VARIANT, Variant.DEFAULT));

		modelBlock = Road.get(rock);
		//noinspection ConstantConditions
		setHarvestLevel(modelBlock.getHarvestTool(modelBlock.getDefaultState()), modelBlock.getHarvestLevel(modelBlock.getDefaultState()));
		useNeighborBrightness = true;
		setLightOpacity(255);
	}

	@Override
	public void onEntityWalk(final World worldIn, final BlockPos pos, final Entity entityIn) {
		modelBlock.onEntityWalk(worldIn, pos, entityIn);
	}

	@Override
	public String getTranslationKey(int meta) {
		return super.getTranslationKey();
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
	@SuppressWarnings("deprecation")
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = this.getDefaultState().withProperty(VARIANT, Variant.DEFAULT);

		if (!this.isDouble())
			state = state.withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);

		return state;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = 0;

		if (!isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP)
			i |= 8;

		return i;
	}

	@Override
	@SuppressWarnings("deprecation")
	public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
		return modelBlock.getBlockHardness(blockState, worldIn, pos);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(halfSlab);
	}

	@Override
	@SuppressWarnings("deprecation")
	public float getExplosionResistance(Entity exploder) {
		return modelBlock.getExplosionResistance(exploder);
	}

	@Override
	@SuppressWarnings("deprecation")
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		return new ItemStack(halfSlab);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return isDouble() ? new BlockStateContainer(this, VARIANT) : new BlockStateContainer(this, HALF, VARIANT);
	}

	@Override
	@SuppressWarnings("deprecation")
	public SoundType getSoundType() {
		return modelBlock.getSoundType();
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

		public final Double doubleSlab;

		public Half(Rock rock) {
			super(rock);

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