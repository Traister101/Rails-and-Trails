package mod.traister101.rnt.objects.blocks;

import net.dries007.tfc.api.types.Rock;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class RoadSlab extends BlockSlab {

	public static final PropertyEnum<Variant> VARIANT = PropertyEnum.create("variant", Variant.class);
	/// The block the slab is a "child" of. IE which block our slab should act like
	public final Block parentBlock;
	protected Half halfSlab;

	public RoadSlab(Rock rock) {
		super(Road.get(rock).getDefaultState().getMaterial());

		IBlockState state = blockState.getBaseState();
		if (!isDouble()) state = state.withProperty(HALF, EnumBlockHalf.BOTTOM);
		setDefaultState(state.withProperty(VARIANT, Variant.DEFAULT));

		parentBlock = Road.get(rock);
		final IBlockState defaultState = parentBlock.getDefaultState();
		//noinspection ConstantConditions
		setHarvestLevel(parentBlock.getHarvestTool(defaultState), parentBlock.getHarvestLevel(defaultState));
		useNeighborBrightness = true;
		setLightOpacity(255);
	}

	@Override
	public String getTranslationKey(final int meta) {
		return super.getTranslationKey();
	}

	@Override
	public IProperty<?> getVariantProperty() {
		return VARIANT;
	}

	@Override
	public Comparable<?> getTypeForItem(final ItemStack stack) {
		return Variant.DEFAULT;
	}

	@Override
	@SuppressWarnings("deprecation")
	public IBlockState getStateFromMeta(final int meta) {
		IBlockState state = this.getDefaultState().withProperty(VARIANT, Variant.DEFAULT);

		if (!this.isDouble()) {
			state = state.withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
		}

		return state;
	}

	@Override
	public int getMetaFromState(final IBlockState blockState) {
		int i = 0;

		if (!isDouble() && blockState.getValue(HALF) == EnumBlockHalf.TOP) {
			i |= 8;
		}

		return i;
	}

	@Override
	@SuppressWarnings("deprecation")
	public float getBlockHardness(final IBlockState blockState, final World world, final BlockPos blockPos) {
		return parentBlock.getBlockHardness(blockState, world, blockPos);
	}

	@Override
	public Item getItemDropped(final IBlockState blockState, final Random rand, final int fortune) {
		return Item.getItemFromBlock(halfSlab);
	}

	@Override
	@SuppressWarnings("deprecation")
	public float getExplosionResistance(final Entity exploder) {
		return parentBlock.getExplosionResistance(exploder);
	}

	@Override
	public void onEntityWalk(final World worldIn, final BlockPos pos, final Entity entityIn) {
		parentBlock.onEntityWalk(worldIn, pos, entityIn);
	}

	@Override
	@SuppressWarnings("deprecation")
	public ItemStack getItem(final World world, final BlockPos blockPos, final IBlockState blockState) {
		return new ItemStack(halfSlab);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return isDouble() ? new BlockStateContainer(this, VARIANT) : new BlockStateContainer(this, HALF, VARIANT);
	}

	@Override
	@SuppressWarnings("deprecation")
	public SoundType getSoundType() {
		return parentBlock.getSoundType();
	}

	public enum Variant implements IStringSerializable {
		DEFAULT;

		@Override
		public String getName() {
			return "default";
		}
	}

	public static class Double extends RoadSlab {

		/// Map containing rock double slab pairs to better manage slabs
		private static final Map<Rock, Double> ROCK_TABLE = new HashMap<>();

		public Double(Rock rock) {
			super(rock);

			ROCK_TABLE.put(rock, this);
		}

		/**
		 * Returns the double slab variant for the rock type
		 *
		 * @param rock type of this rock
		 *
		 * @return Double slab for given rock type
		 */
		public static Double get(final Rock rock) {
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
		}

		@Override
		public boolean isDouble() {
			return false;
		}
	}
}