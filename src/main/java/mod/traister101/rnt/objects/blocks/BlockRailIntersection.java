package mod.traister101.rnt.objects.blocks;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class BlockRailIntersection extends BlockRailBaseRNT {

	public static final PropertyEnum<EnumRailDirection> SHAPE = PropertyEnum.create("shape", EnumRailDirection.class,
			validState -> validState == EnumRailDirection.NORTH_SOUTH || validState == EnumRailDirection.EAST_WEST);

	public BlockRailIntersection() {
		super(true);
		setDefaultState(blockState.getBaseState().withProperty(SHAPE, EnumRailDirection.NORTH_SOUTH));
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
		if (cart == null) return super.getRailDirection(world, blockPos, blockState, cart);

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
	@SuppressWarnings("deprecation")
	public IBlockState getStateFromMeta(final int meta) {
		return super.getStateFromMeta(meta);
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