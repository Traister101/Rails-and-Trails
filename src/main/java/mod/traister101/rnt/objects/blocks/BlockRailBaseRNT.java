package mod.traister101.rnt.objects.blocks;

import net.dries007.tfc.api.capability.size.IItemSize;
import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class BlockRailBaseRNT extends BlockRailBase implements IItemSize {

	public BlockRailBaseRNT(boolean isPowered) {
		super(isPowered);
	}

	@Override
	protected IBlockState updateDir(final World world, final BlockPos blockPos, final IBlockState blockState,
			final boolean initialPlacement) {
		return world.isRemote ? blockState : (new RailRNT(world, blockPos, blockState))
				.place(world.isBlockPowered(blockPos), initialPlacement)
				.getBlockState();
	}

	@Override
	public IBlockState getStateForPlacement(final World world, final BlockPos blockPos, final EnumFacing facing, final float hitX,
			final float hitY, final float hitZ, final int meta,
			final EntityLivingBase placer, final EnumHand hand) {
		switch (placer.getHorizontalFacing()) {
			case NORTH:
			case SOUTH:
				return getDefaultState().withProperty(getShapeProperty(), EnumRailDirection.NORTH_SOUTH);
			case WEST:
			case EAST:
				return getDefaultState().withProperty(getShapeProperty(), EnumRailDirection.EAST_WEST);
		}
		return getDefaultState();
	}

	@Nonnull
	@Override
	public Size getSize(@Nonnull final ItemStack itemStack) {
		return Size.SMALL;
	}

	@Nonnull
	@Override
	public Weight getWeight(@Nonnull final ItemStack itemStack) {
		return Weight.LIGHT;
	}

	/**
	 * This is needed for the slightly improved rail placement our rails provide compared to vanilla.
	 * See {@link BlockRailBaseRNT#updateDir(World, BlockPos, IBlockState, boolean)} where we explicitly
	 * instantiate our rail block instead of vanillas. Otherwise, we inherit the vast majority of the vanilla
	 * placement logic
	 */
	public static class RailRNT {

		private final World world;
		private final BlockPos pos;
		private final BlockRailBase block;
		private final boolean canMakeCorners;
		private final List<BlockPos> connectedRails = new ArrayList<>(2);
		private final boolean canMakeSlopes;
		private IBlockState state;

		public RailRNT(World world, BlockPos pos, IBlockState state) {
			this.world = world;
			this.pos = pos;
			this.state = state;
			this.block = (BlockRailBase) state.getBlock();
			this.canMakeCorners = block.isFlexibleRail(world, pos);
			this.canMakeSlopes = block.canMakeSlopes(world, pos);
			updateConnectedRails(block.getRailDirection(world, pos, state, null));
		}

		/**
		 * Counts the number of rails adjacent to this rail.
		 */
		protected int countAdjacentRails() {
			int i = 0;

			for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
				if (hasRailAt(pos.offset(enumfacing))) {
					++i;
				}
			}

			return i;
		}

		public RailRNT place(boolean powered, boolean initialPlacement) {
			final BlockPos posNorth = pos.north();
			final BlockPos posSouth = pos.south();
			final BlockPos posWest = pos.west();
			final BlockPos posEast = pos.east();
			final boolean neighborRailNorth = hasNeighborRail(posNorth);
			final boolean neighborRailSouth = hasNeighborRail(posSouth);
			final boolean neighborRailWest = hasNeighborRail(posWest);
			final boolean neighborRailEast = hasNeighborRail(posEast);
			EnumRailDirection railDirection = null;

			// If it's the initial placement initialize our rail direction using the current state
			if (initialPlacement) railDirection = state.getValue(block.getShapeProperty());

			if ((neighborRailNorth || neighborRailSouth) && !neighborRailWest && !neighborRailEast) {
				railDirection = EnumRailDirection.NORTH_SOUTH;
			}

			if ((neighborRailWest || neighborRailEast) && !neighborRailNorth && !neighborRailSouth) {
				railDirection = EnumRailDirection.EAST_WEST;
			}

			if (canMakeCorners) {
				if (neighborRailSouth && neighborRailEast && !neighborRailNorth && !neighborRailWest) {
					railDirection = EnumRailDirection.SOUTH_EAST;
				}

				if (neighborRailSouth && neighborRailWest && !neighborRailNorth && !neighborRailEast) {
					railDirection = EnumRailDirection.SOUTH_WEST;
				}

				if (neighborRailNorth && neighborRailWest && !neighborRailSouth && !neighborRailEast) {
					railDirection = EnumRailDirection.NORTH_WEST;
				}

				if (neighborRailNorth && neighborRailEast && !neighborRailSouth && !neighborRailWest) {
					railDirection = EnumRailDirection.NORTH_EAST;
				}
			}

			if (railDirection == null) {
				if (neighborRailNorth || neighborRailSouth) {
					railDirection = EnumRailDirection.NORTH_SOUTH;
				}

				if (neighborRailWest || neighborRailEast) {
					railDirection = EnumRailDirection.EAST_WEST;
				}

				if (canMakeCorners) {
					if (powered) {
						if (neighborRailSouth && neighborRailEast) {
							railDirection = EnumRailDirection.SOUTH_EAST;
						}

						if (neighborRailWest && neighborRailSouth) {
							railDirection = EnumRailDirection.SOUTH_WEST;
						}

						if (neighborRailEast && neighborRailNorth) {
							railDirection = EnumRailDirection.NORTH_EAST;
						}

						if (neighborRailNorth && neighborRailWest) {
							railDirection = EnumRailDirection.NORTH_WEST;
						}
					} else {
						if (neighborRailNorth && neighborRailWest) {
							railDirection = EnumRailDirection.NORTH_WEST;
						}

						if (neighborRailEast && neighborRailNorth) {
							railDirection = EnumRailDirection.NORTH_EAST;
						}

						if (neighborRailWest && neighborRailSouth) {
							railDirection = EnumRailDirection.SOUTH_WEST;
						}

						if (neighborRailSouth && neighborRailEast) {
							railDirection = EnumRailDirection.SOUTH_EAST;
						}
					}
				}
			}

			if (railDirection == EnumRailDirection.EAST_WEST && canMakeSlopes) {
				if (BlockRailBase.isRailBlock(world, posWest.up())) {
					railDirection = EnumRailDirection.ASCENDING_WEST;
				}

				if (BlockRailBase.isRailBlock(world, posEast.up())) {
					railDirection = EnumRailDirection.ASCENDING_EAST;
				}
			}

			if (railDirection == EnumRailDirection.NORTH_SOUTH && canMakeSlopes) {
				if (BlockRailBase.isRailBlock(world, posSouth.up())) {
					railDirection = EnumRailDirection.ASCENDING_SOUTH;
				}

				if (BlockRailBase.isRailBlock(world, posNorth.up())) {
					railDirection = EnumRailDirection.ASCENDING_NORTH;
				}
			}

			if (railDirection == null) {
				return this;
			}

			updateConnectedRails(railDirection);
			state = state.withProperty(block.getShapeProperty(), railDirection);

			if (initialPlacement || world.getBlockState(pos) != state) {
				world.setBlockState(pos, state);

				for (final BlockPos connectedRail : connectedRails) {
					final RailRNT rail = findRailAt(connectedRail);

					if (rail != null) {
						rail.removeSoftConnections();

						if (rail.canConnectTo(this)) {
							rail.connectTo(this);
						}
					}
				}
			}

			return this;
		}

		private void updateConnectedRails(final EnumRailDirection railDirection) {
			connectedRails.clear();

			switch (railDirection) {
				case NORTH_SOUTH:
					connectedRails.add(pos.north());
					connectedRails.add(pos.south());
					break;
				case EAST_WEST:
					connectedRails.add(pos.west());
					connectedRails.add(pos.east());
					break;
				case ASCENDING_EAST:
					connectedRails.add(pos.west());
					connectedRails.add(pos.east().up());
					break;
				case ASCENDING_WEST:
					connectedRails.add(pos.west().up());
					connectedRails.add(pos.east());
					break;
				case ASCENDING_NORTH:
					connectedRails.add(pos.north().up());
					connectedRails.add(pos.south());
					break;
				case ASCENDING_SOUTH:
					connectedRails.add(pos.north());
					connectedRails.add(pos.south().up());
					break;
				case SOUTH_EAST:
					connectedRails.add(pos.east());
					connectedRails.add(pos.south());
					break;
				case SOUTH_WEST:
					connectedRails.add(pos.west());
					connectedRails.add(pos.south());
					break;
				case NORTH_WEST:
					connectedRails.add(pos.west());
					connectedRails.add(pos.north());
					break;
				case NORTH_EAST:
					connectedRails.add(pos.east());
					connectedRails.add(pos.north());
			}
		}

		private void removeSoftConnections() {
			for (int i = 0; i < connectedRails.size(); ++i) {
				final RailRNT rail = findRailAt(connectedRails.get(i));

				if (rail != null && rail.isConnectedToRail(this)) {
					connectedRails.set(i, rail.pos);
					continue;
				}

				connectedRails.remove(i--);
			}
		}

		private boolean hasRailAt(final BlockPos pos) {
			return BlockRailBase.isRailBlock(world, pos) || BlockRailBase.isRailBlock(world,
					pos.up()) || BlockRailBase.isRailBlock(world, pos.down());
		}

		@Nullable
		private RailRNT findRailAt(final BlockPos blockPos) {
			final IBlockState blockState = world.getBlockState(blockPos);

			// Found rail at the given location
			if (BlockRailBase.isRailBlock(blockState)) return new RailRNT(world, blockPos, blockState);

			final BlockPos blockPosUp = blockPos.up();
			final IBlockState blockStateUp = world.getBlockState(blockPosUp);
			// Found rail one block up
			if (BlockRailBase.isRailBlock(blockStateUp)) return new RailRNT(world, blockPosUp, blockStateUp);

			final BlockPos blockPosDown = blockPos.down();
			final IBlockState blockStateDown = world.getBlockState(blockPosDown);
			// Found rail one block down
			if (BlockRailBase.isRailBlock(blockStateDown)) return new RailRNT(world, blockPosDown, blockStateDown);

			return null;
		}

		private boolean isConnectedToRail(final RailRNT rail) {
			return isConnectedTo(rail.pos);
		}

		private boolean isConnectedTo(final BlockPos posIn) {
			for (final BlockPos blockpos : connectedRails) {
				if (blockpos.getX() == posIn.getX() && blockpos.getZ() == posIn.getZ()) {
					return true;
				}
			}

			return false;
		}

		private boolean canConnectTo(final RailRNT rail) {
			return isConnectedToRail(rail) || connectedRails.size() != 2;
		}

		private void connectTo(final RailRNT rail) {
			connectedRails.add(rail.pos);
			final BlockPos blockPosNorth = pos.north();
			final BlockPos blockPosSouth = pos.south();
			final BlockPos blockPosWest = pos.west();
			final BlockPos blockPosEast = pos.east();
			final boolean connectedToNorth = isConnectedTo(blockPosNorth);
			final boolean connectedToSouth = isConnectedTo(blockPosSouth);
			final boolean connectedToWest = isConnectedTo(blockPosWest);
			final boolean connectedToEast = isConnectedTo(blockPosEast);
			EnumRailDirection railDirection = null;

			if (connectedToNorth || connectedToSouth) {
				railDirection = EnumRailDirection.NORTH_SOUTH;
			}

			if (connectedToWest || connectedToEast) {
				railDirection = EnumRailDirection.EAST_WEST;
			}

			if (canMakeCorners) {
				if (connectedToSouth && connectedToEast && !connectedToNorth && !connectedToWest) {
					railDirection = EnumRailDirection.SOUTH_EAST;
				}

				if (connectedToSouth && connectedToWest && !connectedToNorth && !connectedToEast) {
					railDirection = EnumRailDirection.SOUTH_WEST;
				}

				if (connectedToNorth && connectedToWest && !connectedToSouth && !connectedToEast) {
					railDirection = EnumRailDirection.NORTH_WEST;
				}

				if (connectedToNorth && connectedToEast && !connectedToSouth && !connectedToWest) {
					railDirection = EnumRailDirection.NORTH_EAST;
				}
			}

			if (railDirection == EnumRailDirection.EAST_WEST && canMakeSlopes) {
				if (BlockRailBase.isRailBlock(world, blockPosEast.up())) {
					railDirection = EnumRailDirection.ASCENDING_EAST;
				}

				if (BlockRailBase.isRailBlock(world, blockPosWest.up())) {
					railDirection = EnumRailDirection.ASCENDING_WEST;
				}
			}

			if (railDirection == EnumRailDirection.NORTH_SOUTH && canMakeSlopes) {
				if (BlockRailBase.isRailBlock(world, blockPosNorth.up())) {
					railDirection = EnumRailDirection.ASCENDING_NORTH;
				}

				if (BlockRailBase.isRailBlock(world, blockPosSouth.up())) {
					railDirection = EnumRailDirection.ASCENDING_SOUTH;
				}
			}

			if (railDirection == null) {
				railDirection = EnumRailDirection.NORTH_SOUTH;
			}

			state = state.withProperty(block.getShapeProperty(), railDirection);
			world.setBlockState(pos, state, 3);
		}

		private boolean hasNeighborRail(final BlockPos posIn) {
			final RailRNT rail = findRailAt(posIn);

			if (rail == null) {
				return false;
			} else {
				rail.removeSoftConnections();
				return rail.canConnectTo(this);
			}
		}

		public IBlockState getBlockState() {
			return state;
		}

		public List<BlockPos> getConnectedRails() {
			return connectedRails;
		}
	}
}
