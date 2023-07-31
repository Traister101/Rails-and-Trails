package mod.traister101.rnt.objects.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockFlexibleRail extends BlockRailBaseRNT {

	public static final PropertyEnum<EnumRailDirection> SHAPE = PropertyEnum.create("shape", BlockRailBase.EnumRailDirection.class);

	protected BlockFlexibleRail() {
		super(false);
		setDefaultState(blockState.getBaseState()
				.withProperty(SHAPE, EnumRailDirection.NORTH_SOUTH));
	}

	@Override
	protected void updateState(final IBlockState blockState, final World world, final BlockPos blockPos, final Block block) {
		if (block.getDefaultState().canProvidePower() && (new RailRNT(world, blockPos, blockState)).countAdjacentRails() == 3) {
			updateDir(world, blockPos, blockState, false);
		}
	}

	@Override
	public IProperty<EnumRailDirection> getShapeProperty() {
		return SHAPE;
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
	@SuppressWarnings("deprecation")
	public IBlockState withRotation(final IBlockState blockState, final Rotation rotation) {
		final EnumRailDirection railDirection = blockState.getValue(SHAPE);

		switch (rotation) {
			case CLOCKWISE_180:
				switch (railDirection) {
					case ASCENDING_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_WEST);

					case ASCENDING_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_EAST);

					case ASCENDING_NORTH:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_SOUTH);

					case ASCENDING_SOUTH:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_NORTH);

					case SOUTH_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.NORTH_WEST);

					case SOUTH_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.NORTH_EAST);

					case NORTH_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.SOUTH_EAST);

					case NORTH_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.SOUTH_WEST);
				}

			case COUNTERCLOCKWISE_90:
				switch (railDirection) {
					case ASCENDING_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_NORTH);

					case ASCENDING_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_SOUTH);

					case ASCENDING_NORTH:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_WEST);

					case ASCENDING_SOUTH:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_EAST);

					case SOUTH_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.NORTH_EAST);

					case SOUTH_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.SOUTH_EAST);

					case NORTH_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.SOUTH_WEST);

					case NORTH_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.NORTH_WEST);

					case NORTH_SOUTH:
						return blockState.withProperty(SHAPE, EnumRailDirection.EAST_WEST);

					case EAST_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.NORTH_SOUTH);
				}

			case CLOCKWISE_90:
				switch (railDirection) {
					case ASCENDING_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_SOUTH);

					case ASCENDING_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_NORTH);

					case ASCENDING_NORTH:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_EAST);

					case ASCENDING_SOUTH:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_WEST);

					case SOUTH_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.SOUTH_WEST);

					case SOUTH_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.NORTH_WEST);

					case NORTH_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.NORTH_EAST);

					case NORTH_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.SOUTH_EAST);

					case NORTH_SOUTH:
						return blockState.withProperty(SHAPE, EnumRailDirection.EAST_WEST);

					case EAST_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.NORTH_SOUTH);
				}

			case NONE:
				return blockState;
		}
		return blockState;
	}

	@Override
	@SuppressWarnings("deprecation")
	public IBlockState withMirror(final IBlockState blockState, final Mirror mirror) {
		final EnumRailDirection railDirection = blockState.getValue(SHAPE);

		switch (mirror) {
			case LEFT_RIGHT:
				switch (railDirection) {
					case ASCENDING_NORTH:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_SOUTH);

					case ASCENDING_SOUTH:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_NORTH);

					case SOUTH_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.NORTH_EAST);

					case SOUTH_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.NORTH_WEST);

					case NORTH_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.SOUTH_WEST);

					case NORTH_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.SOUTH_EAST);

					case NORTH_SOUTH:
					case EAST_WEST:
					case ASCENDING_EAST:
					case ASCENDING_WEST:
						return blockState;
				}

			case FRONT_BACK:
				switch (railDirection) {
					case ASCENDING_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_WEST);
					case ASCENDING_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_EAST);

					case SOUTH_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.SOUTH_EAST);

					case NORTH_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.NORTH_EAST);

					case NORTH_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.NORTH_WEST);

					case SOUTH_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.SOUTH_WEST);

					case NORTH_SOUTH:
					case EAST_WEST:
					case ASCENDING_NORTH:
					case ASCENDING_SOUTH:
						return blockState;
				}

			case NONE:
				return blockState;
		}
		return blockState;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, SHAPE);
	}
}