package mod.traister101.rnt.objects.entities;

import mcp.MethodsReturnNonnullByDefault;
import mod.traister101.rnt.objects.items.ItemsRNT;
import net.dries007.tfc.objects.blocks.wood.BlockBarrel;
import net.dries007.tfc.objects.blocks.wood.BlockChestTFC;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EntitySteelMinecart extends EntityMinecartEmpty {

	private static final int[][][] MATRIX = new int[][][] {{{0, 0, -1}, {0, 0, 1}}, {{-1, 0, 0}, {1, 0, 0}}, {{-1, -1, 0}, {1, 0, 0}}, {{-1, 0, 0}, {1, -1, 0}}, {{0, 0, -1}, {0, -1, 1}}, {{0, -1, -1}, {0, 0, 1}}, {{0, 0, 1}, {1, 0, 0}}, {{0, 0, 1}, {-1, 0, 0}}, {{0, 0, -1}, {-1, 0, 0}}, {{0, 0, -1}, {1, 0, 0}}};

	@SuppressWarnings("unused")
	public EntitySteelMinecart(World worldIn) {
		super(worldIn);
	}

	public EntitySteelMinecart(World worldIn, double x, double y, double z) {
		super(worldIn, x, y, z);
	}

	/**
	 * Helper that handles the slightly unintuitive minecart placement on rails
	 *
	 * @param world         World the entity is given
	 * @param x             X position of the minecart
	 * @param y             Y position of the minecart
	 * @param z             Z position of the minecart
	 * @param railDirection Enum for the rail we are spawning the minecart onto
	 * @return A new minecart entity
	 */
	public static EntitySteelMinecart create(final World world, final double x, final double y, final double z,
			final EnumRailDirection railDirection) {
		final double xOffset;
		final double yOffset = railDirection.isAscending() ? 0.5 : 0;
		final double zOffset;
		switch (railDirection) {
			case SOUTH_EAST:
				xOffset = 0.75;
				zOffset = 0.75;
				break;
			case SOUTH_WEST:
				xOffset = 0.25;
				zOffset = 0.75;
				break;
			case NORTH_WEST:
				xOffset = 0.25;
				zOffset = 0.25;
				break;
			case NORTH_EAST:
				xOffset = 0.75;
				zOffset = 0.25;
				break;
			default:
				xOffset = 0.5;
				zOffset = 0.5;
		}

		return new EntitySteelMinecart(world, x + xOffset, y + 0.0625 + yOffset, z + zOffset);
	}

	@Override
	public boolean processInitialInteract(final EntityPlayer player, final EnumHand hand) {
		// Have to be sneaking for our in world container attaching
		if (!player.isSneaking()) return super.processInitialInteract(player, hand);
		final ItemStack heldStack = player.getHeldItem(EnumHand.MAIN_HAND);

		// The held stack isn't a block
		if (!(heldStack.getItem() instanceof ItemBlock)) return super.processInitialInteract(player, hand);

		final ItemBlock itemBlock = (ItemBlock) heldStack.getItem();
		final Block block = itemBlock.getBlock();
		if (block instanceof BlockChestTFC) {
			// Wrong kind of chest
			if (((BlockChestTFC) block).chestType == BlockChest.Type.TRAP) return super.processInitialInteract(player, hand);
			// TODO spawn a chest minecart and kill this one
			setCustomNameTag("Chest Minecart");
			return true;
		}

		if (block instanceof BlockBarrel) {
			// TODO spawn a barrel minecart and kill this one
			setCustomNameTag("Barrel Minecart");
			return true;
		}

		return super.processInitialInteract(player, hand);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
	}

	@Override
	protected void moveAlongTrack(final BlockPos blockPos, final IBlockState state) {
		fallDistance = 0;
		final Vec3d startPos = getPos(posX, posY, posZ);
		posY = blockPos.getY();

		final BlockRailBase railBlock = (BlockRailBase) state.getBlock();
		final EnumRailDirection railDirection = railBlock.getRailDirection(world, blockPos, state, this);
		// Handle sloped rails
		switch (railDirection) {
			case ASCENDING_EAST:
				motionX -= getSlopeAdjustment();
				++posY;
				break;
			case ASCENDING_WEST:
				motionX += getSlopeAdjustment();
				++posY;
				break;
			case ASCENDING_NORTH:
				motionZ += getSlopeAdjustment();
				++posY;
				break;
			case ASCENDING_SOUTH:
				motionZ -= getSlopeAdjustment();
				++posY;
		}

		// TODO clean up the entire function more, at the very least give all the variables actual names
		final int[][] aint = MATRIX[railDirection.getMetadata()];
		double d1 = (aint[1][0] - aint[0][0]);
		double d2 = (aint[1][2] - aint[0][2]);
		final double d3 = Math.sqrt(d1 * d1 + d2 * d2);
		final double d4 = motionX * d1 + motionZ * d2;

		if (d4 < 0.0D) {
			d1 = -d1;
			d2 = -d2;
		}

		final double d5 = Math.min(Math.sqrt(motionX * motionX + motionZ * motionZ), 2);

		motionX = d5 * d1 / d3;
		motionZ = d5 * d2 / d3;

		final boolean accelerate;
		final boolean decelerate;
		{// Scope that figures out if we should accelerate or decelerate
			boolean deferDecelerate = false;
			// Powered rails, should include rails of all mods
			if (railBlock instanceof BlockRailPowered) {
				accelerate = state.getValue(BlockRailPowered.POWERED);
				deferDecelerate = !accelerate;
			} else {
				accelerate = false;
			}

			final Entity entity = getPassengers().isEmpty() ? null : getPassengers().get(0);
			// We have a passenger
			if (entity instanceof EntityLivingBase) {
				final double pilotForwardSpeed = ((EntityLivingBase) entity).moveForward;

				if (pilotForwardSpeed > 0) {
					final double direction = motionX * motionX + motionZ * motionZ;

					if (direction < 0.01) {
						final double d7 = -Math.sin((entity.rotationYaw * 0.017453292));
						final double d8 = Math.cos((entity.rotationYaw * 0.017453292));
						motionX += d7 * 0.1;
						motionZ += d8 * 0.1;
						deferDecelerate = false;
					}
				}
			}
			decelerate = deferDecelerate;
		}

		if (decelerate && shouldDoRailFunctions()) {
			final double speed = Math.sqrt(motionX * motionX + motionZ * motionZ);

			if (speed < 0.03) {
				motionX *= 0;
				motionY *= 0;
				motionZ *= 0;
			} else {
				motionX *= 0.5;
				motionY *= 0;
				motionZ *= 0.5;
			}
		}

		final double d18 = (double) blockPos.getX() + 0.5 + (double) aint[0][0] * 0.5;
		final double d19 = (double) blockPos.getZ() + 0.5 + (double) aint[0][2] * 0.5;
		final double d20 = (double) blockPos.getX() + 0.5 + (double) aint[1][0] * 0.5;
		final double d21 = (double) blockPos.getZ() + 0.5 + (double) aint[1][2] * 0.5;
		d1 = d20 - d18;
		d2 = d21 - d19;

		final double d10;
		if (d1 == 0) {
			posX = (double) blockPos.getX() + 0.5;
			d10 = posZ - (double) blockPos.getZ();
		} else if (d2 == 0.0) {
			posZ = (double) blockPos.getZ() + 0.5;
			d10 = posX - (double) blockPos.getX();
		} else {
			final double d11 = posX - d18;
			final double d12 = posZ - d19;
			d10 = (d11 * d1 + d12 * d2) * 2;
		}

		posX = d18 + d1 * d10;
		posZ = d19 + d2 * d10;
		setPosition(posX, posY, posZ);
		moveMinecartOnRail(blockPos);

		if (aint[0][1] != 0 && MathHelper.floor(posX) - blockPos.getX() == aint[0][0] && MathHelper.floor(
				posZ) - blockPos.getZ() == aint[0][2]) {
			setPosition(posX, posY + (double) aint[0][1], posZ);
		} else if (aint[1][1] != 0 && MathHelper.floor(posX) - blockPos.getX() == aint[1][0] && MathHelper.floor(
				posZ) - blockPos.getZ() == aint[1][2]) {
			setPosition(posX, posY + (double) aint[1][1], posZ);
		}

		applyDrag();
		final Vec3d endPos = getPos(posX, posY, posZ);

		if (endPos != null && startPos != null) {
			final double distanceTraveled = (startPos.y - endPos.y) * 0.05D;
			final double d23 = Math.sqrt(motionX * motionX + motionZ * motionZ);

			if (d23 > 0.0D) {
				motionX = motionX / d23 * (d23 + distanceTraveled);
				motionZ = motionZ / d23 * (d23 + distanceTraveled);
			}

			setPosition(posX, endPos.y, posZ);
		}

		final int blockPosX = MathHelper.floor(posX);
		final int blockPosZ = MathHelper.floor(posZ);

		if (blockPosX != blockPos.getX() || blockPosZ != blockPos.getZ()) {
			final double speed = Math.sqrt(motionX * motionX + motionZ * motionZ);
			motionX = speed * (double) (blockPosX - blockPos.getX());
			motionZ = speed * (double) (blockPosZ - blockPos.getZ());
		}

		if (shouldDoRailFunctions()) {
			((BlockRailBase) state.getBlock()).onMinecartPass(world, this, blockPos);
		}

		if (accelerate && shouldDoRailFunctions()) {
			final double speed = Math.sqrt(motionX * motionX + motionZ * motionZ);

			if (speed > 0.01D) {
				motionX += motionX / speed * 0.06D;
				motionZ += motionZ / speed * 0.06D;
			} else if (railDirection == BlockRailBase.EnumRailDirection.EAST_WEST) {
				if (world.getBlockState(blockPos.west()).isNormalCube()) {
					motionX = 0.02D;
				} else if (world.getBlockState(blockPos.east()).isNormalCube()) {
					motionX = -0.02D;
				}
			} else if (railDirection == BlockRailBase.EnumRailDirection.NORTH_SOUTH) {
				if (world.getBlockState(blockPos.north()).isNormalCube()) {
					motionZ = 0.02D;
				} else if (world.getBlockState(blockPos.south()).isNormalCube()) {
					motionZ = -0.02D;
				}
			}
		}
	}

	@Override
	protected double getMaximumSpeed() {
		return super.getMaximumSpeed() * 1.5;
	}

	@Override
	public ItemStack getCartItem() {
		return new ItemStack(ItemsRNT.STEEL_MINECART);
	}

	// This needs to be overridden so our minecart item drops
	@Override
	public void killMinecart(final DamageSource source) {
		setDead();

		if (world.getGameRules().getBoolean("doEntityDrops")) {
			final ItemStack itemstack = new ItemStack(ItemsRNT.STEEL_MINECART);

			if (hasCustomName()) itemstack.setStackDisplayName(getCustomNameTag());

			entityDropItem(itemstack, 0);
		}
	}
}