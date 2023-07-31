package mod.traister101.rnt.objects.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAcceleratorRail extends BlockRailBaseRNT {

	public static final PropertyEnum<EnumRailDirection> SHAPE = PropertyEnum.create("shape", EnumRailDirection.class,
			validStates -> validStates != EnumRailDirection.NORTH_EAST && validStates != EnumRailDirection.NORTH_WEST &&
					validStates != EnumRailDirection.SOUTH_EAST && validStates != EnumRailDirection.SOUTH_WEST);
	public static final PropertyBool POWERED = PropertyBool.create("powered");

	public BlockAcceleratorRail() {
		super(true);
		setDefaultState(blockState.getBaseState()
				.withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH)
				.withProperty(POWERED, false));
	}

	@Override
	protected void updateState(final IBlockState blockState, final World world, final BlockPos blockPos, final Block block) {
		final boolean poweredState = blockState.getValue(POWERED);
		final boolean poweredWorld = world.isBlockPowered(blockPos) || findPoweredRailSignal(world, blockPos, blockState, true,
				0) || findPoweredRailSignal(world, blockPos, blockState, false, 0);

		if (poweredWorld != poweredState) {
			world.setBlockState(blockPos, blockState.withProperty(POWERED, poweredWorld));
			world.notifyNeighborsOfStateChange(blockPos.down(), this, false);

			if (blockState.getValue(SHAPE).isAscending()) {
				world.notifyNeighborsOfStateChange(blockPos.up(), this, false);
			}
		}
	}

	public IProperty<EnumRailDirection> getShapeProperty() {
		return SHAPE;
	}

	@Override
	public float getRailMaxSpeed(final World world, final EntityMinecart cart, final BlockPos blockPos) {
		return super.getRailMaxSpeed(world, cart, blockPos) * 1.5f;
	}

	protected boolean findPoweredRailSignal(final World world, final BlockPos blockPos, final IBlockState blockState,
			final boolean backwards, final int redstonePower) {
		if (redstonePower >= 8) return false;

		int posX = blockPos.getX();
		int posY = blockPos.getY();
		int posZ = blockPos.getZ();
		EnumRailDirection railDirection = blockState.getValue(SHAPE);

		final boolean flag;
		switch (railDirection) {
			case NORTH_SOUTH:
				if (backwards) {
					posZ++;
				} else {
					posZ--;
				}
				flag = true;
				break;

			case EAST_WEST:
				if (backwards) {
					posX--;
				} else {
					posX++;
				}
				flag = true;
				break;

			case ASCENDING_EAST:
				if (backwards) {
					posX--;
					flag = true;
				} else {
					posX++;
					posY++;
					flag = false;
				}
				railDirection = EnumRailDirection.EAST_WEST;
				break;

			case ASCENDING_WEST:
				if (backwards) {
					posX--;
					posY++;
					flag = false;
				} else {
					posX++;
					flag = true;
				}
				railDirection = EnumRailDirection.EAST_WEST;
				break;

			case ASCENDING_NORTH:
				if (backwards) {
					posZ++;
					flag = true;
				} else {
					posZ--;
					posY++;
					flag = false;
				}
				railDirection = EnumRailDirection.NORTH_SOUTH;
				break;

			case ASCENDING_SOUTH:
				if (backwards) {
					posZ++;
					posY++;
					flag = false;
				} else {
					posZ--;
					flag = true;
				}
				railDirection = EnumRailDirection.NORTH_SOUTH;
				break;

			case SOUTH_EAST:
			case SOUTH_WEST:
			case NORTH_WEST:
			case NORTH_EAST:
			default:
				flag = true;
				break;
		}

		if (isSameRailWithPower(world, new BlockPos(posX, posY, posZ), backwards, redstonePower, railDirection)) {
			return true;
		}
		return flag && isSameRailWithPower(world, new BlockPos(posX, posY - 1, posZ), backwards, redstonePower, railDirection);
	}

	protected boolean isSameRailWithPower(final World world, final BlockPos blockPos, final boolean backwards, final int distance,
			final EnumRailDirection otherRailDirection) {
		final IBlockState blockState = world.getBlockState(blockPos);

		// Not one of ours
		if (!(blockState.getBlock() instanceof BlockAcceleratorRail)) return false;

		final EnumRailDirection railDirection = blockState.getValue(SHAPE);

		// Check if we could connect
		if (otherRailDirection == EnumRailDirection.EAST_WEST) {
			if (railDirection == EnumRailDirection.NORTH_SOUTH) return false;
			if (railDirection == EnumRailDirection.ASCENDING_NORTH) return false;
			if (railDirection == EnumRailDirection.ASCENDING_SOUTH) return false;
		}

		// Check if we could connect
		if (otherRailDirection == EnumRailDirection.NORTH_SOUTH) {
			if (railDirection == EnumRailDirection.EAST_WEST) return false;
			if (railDirection == EnumRailDirection.ASCENDING_EAST) return false;
			if (railDirection == EnumRailDirection.ASCENDING_WEST) return false;
		}

		if (!blockState.getValue(POWERED)) return false;

		return world.isBlockPowered(blockPos) || findPoweredRailSignal(world, blockPos, blockState, backwards, distance + 1);
	}

	@Override
	@SuppressWarnings("deprecation")
	public IBlockState getStateFromMeta(final int meta) {
		return getDefaultState()
				.withProperty(SHAPE, EnumRailDirection.byMetadata(meta & 7))
				.withProperty(POWERED, (meta & 8) > 0);
	}

	@Override
	public int getMetaFromState(final IBlockState blockState) {
		int meta = 0;
		meta = meta | blockState.getValue(SHAPE).getMetadata();

		if (blockState.getValue(POWERED)) meta |= 8;

		return meta;
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
					case NORTH_SOUTH:
						return blockState.withProperty(SHAPE, EnumRailDirection.EAST_WEST);
					case EAST_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.NORTH_SOUTH);
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
				}

			case CLOCKWISE_90:
				switch (railDirection) {
					case NORTH_SOUTH:
						return blockState.withProperty(SHAPE, EnumRailDirection.EAST_WEST);
					case EAST_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.NORTH_SOUTH);
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
					default:
						return blockState;
				}

			case FRONT_BACK:

				switch (railDirection) {
					case ASCENDING_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_WEST);
					case ASCENDING_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.ASCENDING_EAST);
					case SOUTH_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.SOUTH_WEST);
					case SOUTH_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.SOUTH_EAST);
					case NORTH_WEST:
						return blockState.withProperty(SHAPE, EnumRailDirection.NORTH_EAST);
					case NORTH_EAST:
						return blockState.withProperty(SHAPE, EnumRailDirection.NORTH_WEST);
					default:
						return blockState;
				}

			case NONE:
				return blockState;
		}
		return blockState;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, SHAPE, POWERED);
	}
}