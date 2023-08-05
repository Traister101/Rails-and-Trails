package mod.traister101.rnt.objects.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockRailIntersection extends BlockRailBaseRNT {

	public static final PropertyEnum<EnumRailDirection> SHAPE = PropertyEnum.create("shape", EnumRailDirection.class,
			validState -> validState == EnumRailDirection.NORTH_SOUTH || validState == EnumRailDirection.EAST_WEST);

	public BlockRailIntersection() {
		super(true);
		setDefaultState(blockState.getBaseState().withProperty(SHAPE, EnumRailDirection.NORTH_SOUTH));
	}

	@Override
	public void onBlockAdded(final World world, final BlockPos blockPos, final IBlockState blockState) {
		super.onBlockAdded(world, blockPos, blockState);
		super.onBlockAdded(world, blockPos, blockState.cycleProperty(SHAPE));
	}

	@Override
	public void neighborChanged(final IBlockState blockState, final World world, final BlockPos thisBlockPos, final Block block,
			final BlockPos otherBlockPos) {
		updateDir(world, thisBlockPos, blockState, false);
		updateDir(world, thisBlockPos, blockState.cycleProperty(SHAPE), false);
		super.neighborChanged(blockState, world, thisBlockPos, block, otherBlockPos);
	}

	@Override
	public IProperty<EnumRailDirection> getShapeProperty() {
		return SHAPE;
	}

	@Override
	public boolean canMakeSlopes(final IBlockAccess world, final BlockPos pos) {
		return false;
	}

	@Override
	public EnumRailDirection getRailDirection(final IBlockAccess world, final BlockPos blockPos, final IBlockState blockState,
			@Nullable final EntityMinecart cart) {
		if (cart == null) return super.getRailDirection(world, blockPos, blockState, null);

		if (cart.motionX != 0) {
			return EnumRailDirection.EAST_WEST;
		}

		//noinspection IfStatementWithIdenticalBranches
		if (cart.motionZ != 0) {
			return EnumRailDirection.NORTH_SOUTH;
		}

		return EnumRailDirection.NORTH_SOUTH;
	}

	@Override
	public void onMinecartPass(final World world, final EntityMinecart cart, final BlockPos blockPos) {
		final IBlockState blockState = world.getBlockState(blockPos);
		final EnumRailDirection railDirection = getRailDirection(world, blockPos, blockState, cart);
		if (railDirection != blockState.getValue(SHAPE)) world.setBlockState(blockPos, blockState.cycleProperty(SHAPE));
	}

	@Override
	@SuppressWarnings("deprecation")
	public IBlockState getStateFromMeta(final int meta) {
		return getDefaultState().withProperty(SHAPE, EnumRailDirection.byMetadata(meta));
	}

	@Override
	public int getMetaFromState(final IBlockState blockState) {
		return blockState.getValue(SHAPE).getMetadata();
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, SHAPE);
	}
}