package mod.traister101.rnt.objects.entities;

import io.netty.buffer.ByteBuf;
import mod.traister101.rnt.objects.blocks.BlockAcceleratorRail;
import mod.traister101.rnt.objects.items.ItemsRNT;
import mod.traister101.rnt.objects.types.MinecartMetal;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public abstract class EntityMinecartRNT extends EntityMinecart implements IEntityAdditionalSpawnData {

	private static final int[][][] MATRIX = new int[][][] {{{0, 0, -1}, {0, 0, 1}}, {{-1, 0, 0}, {1, 0, 0}}, {{-1, -1, 0}, {1, 0, 0}}, {{-1, 0, 0}, {1, -1, 0}}, {{0, 0, -1}, {0, -1, 1}}, {{0, -1, -1}, {0, 0, 1}}, {{0, 0, 1}, {1, 0, 0}}, {{0, 0, 1}, {-1, 0, 0}}, {{0, 0, -1}, {-1, 0, 0}}, {{0, 0, -1}, {1, 0, 0}}};
	protected MinecartMetal metal;

	@SuppressWarnings("unused")
	public EntityMinecartRNT(World worldIn) {
		super(worldIn);
	}

	public EntityMinecartRNT(World worldIn, double x, double y, double z, final MinecartMetal metal) {
		super(worldIn, x, y, z);
		this.metal = metal;
	}

	public static Vec3d getPlacementPosOffset(final EnumRailDirection railDirection) {
		final double xOffset;
		final double yOffset = (railDirection.isAscending() ? 0.5 : 0) + 0.0625;
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

		return new Vec3d(xOffset, yOffset, zOffset);
	}

	// This needs to be overridden so our minecart item drops

	@Override
	public void killMinecart(final DamageSource source) {
		setDead();

		if (world.getGameRules().getBoolean("doEntityDrops")) {
			final ItemStack itemStack = new ItemStack(ItemsRNT.STEEL_MINECART);

			if (hasCustomName()) itemStack.setStackDisplayName(getCustomNameTag());

			entityDropItem(itemStack, 0);
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
	}

	@Override
	protected double getMaximumSpeed() {
		return super.getMaximumSpeed() * 1.5;
	}

	@Override
	public void onActivatorRailPass(final int x, final int y, final int z, final boolean receivingPower) {
		if (receivingPower) {
			if (isBeingRidden()) {
				removePassengers();
			}

			if (getRollingAmplitude() == 0) {
				setRollingDirection(-getRollingDirection());
				setRollingAmplitude(10);
				setDamage(50);
				markVelocityChanged();
			}
		}
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
		final int[][] rotationForRail = MATRIX[railDirection.getMetadata()];
		{
			double velocityX = (rotationForRail[1][0] - rotationForRail[0][0]);
			double velocityZ = (rotationForRail[1][2] - rotationForRail[0][2]);
			final double speedFactor = Math.sqrt(velocityX * velocityX + velocityZ * velocityZ);
			final double motionSign = motionX * velocityX + motionZ * velocityZ;

			// If negative we need to invert
			if (motionSign < 0) {
				velocityX = -velocityX;
				velocityZ = -velocityZ;
			}

			final double speed = Math.min(Math.sqrt(motionX * motionX + motionZ * motionZ), 2);

			motionX = speed * velocityX / speedFactor;
			motionZ = speed * velocityZ / speedFactor;
		}

		final boolean accelerate;
		final boolean decelerate;
		{// Scope that figures out if we should accelerate or decelerate
			boolean deferDecelerate = false;
			// Our mods powered rails, sorry vanilla and other mods
			if (railBlock instanceof BlockAcceleratorRail) {
				accelerate = state.getValue(BlockAcceleratorRail.POWERED);
				deferDecelerate = !accelerate;
			} else {
				accelerate = false;
			}

			final Entity entity = getPassengers().isEmpty() ? null : getPassengers().get(0);
			// We have a passenger
			if (entity instanceof EntityLivingBase) {
				final double pilotForwardSpeed = ((EntityLivingBase) entity).moveForward;

				if (pilotForwardSpeed > 0) {
					final double speed = motionX * motionX + motionZ * motionZ;

					if (speed < 0.01) {
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

		final double d18 = blockPos.getX() + 0.5 + rotationForRail[0][0] * 0.5;
		final double d19 = blockPos.getZ() + 0.5 + rotationForRail[0][2] * 0.5;
		final double d20 = blockPos.getX() + 0.5 + rotationForRail[1][0] * 0.5;
		final double d21 = blockPos.getZ() + 0.5 + rotationForRail[1][2] * 0.5;
		final double d1 = d20 - d18;
		final double d2 = d21 - d19;

		final double d10;
		if (d1 == 0) {
			posX = blockPos.getX() + 0.5;
			d10 = posZ - blockPos.getZ();
		} else if (d2 == 0) {
			posZ = blockPos.getZ() + 0.5;
			d10 = posX - blockPos.getX();
		} else {
			final double d11 = posX - d18;
			final double d12 = posZ - d19;
			d10 = (d11 * d1 + d12 * d2) * 2;
		}

		posX = d18 + d1 * d10;
		posZ = d19 + d2 * d10;
		setPosition(posX, posY, posZ);
		moveMinecartOnRail(blockPos);

		if (rotationForRail[0][1] != 0 && MathHelper.floor(posX) - blockPos.getX() == rotationForRail[0][0] && MathHelper.floor(
				posZ) - blockPos.getZ() == rotationForRail[0][2]) {
			setPosition(posX, posY + rotationForRail[0][1], posZ);
		} else if (rotationForRail[1][1] != 0 && MathHelper.floor(posX) - blockPos.getX() == rotationForRail[1][0] && MathHelper.floor(
				posZ) - blockPos.getZ() == rotationForRail[1][2]) {
			setPosition(posX, posY + rotationForRail[1][1], posZ);
		}

		applyDrag();
		final Vec3d endPos = getPos(posX, posY, posZ);

		if (endPos != null && startPos != null) {
			final double distanceTraveled = (startPos.y - endPos.y) * 0.05D;
			final double speed = Math.sqrt(motionX * motionX + motionZ * motionZ);

			if (speed > 0.0D) {
				motionX = motionX / speed * (speed + distanceTraveled);
				motionZ = motionZ / speed * (speed + distanceTraveled);
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
				return;
			}

			if (railDirection == EnumRailDirection.EAST_WEST) {
				if (world.getBlockState(blockPos.west()).isNormalCube()) {
					motionX = 0.02D;
					return;
				}

				if (world.getBlockState(blockPos.east()).isNormalCube()) {
					motionX = -0.02D;
					return;
				}

				return;
			}

			if (railDirection == EnumRailDirection.NORTH_SOUTH) {
				if (world.getBlockState(blockPos.north()).isNormalCube()) {
					motionZ = 0.02D;
					return;
				}

				if (world.getBlockState(blockPos.south()).isNormalCube()) {
					motionZ = -0.02D;
				}
			}
		}
	}

	@Override
	protected void readEntityFromNBT(final NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		metal = MinecartMetal.values()[compound.getByte("Metal")];
	}

	@Override
	protected void writeEntityToNBT(final NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setByte("Metal", (byte) metal.ordinal());
	}

	@Override
	public ItemStack getCartItem() {
		switch (metal) {
			case BRONZE:
				return new ItemStack(ItemsRNT.BRONZE_MINECART);
			case WROUGHT_IRON:
				return new ItemStack(ItemsRNT.WROUGHT_IRON_MINECART);
			case STEEL:
				return new ItemStack(ItemsRNT.STEEL_MINECART);
			default:
				return ItemStack.EMPTY;
		}
	}

	@Override
	public void writeSpawnData(final ByteBuf buffer) {
		buffer.writeByte(metal.ordinal());
	}

	@Override
	public void readSpawnData(final ByteBuf additionalData) {
		metal = MinecartMetal.values()[additionalData.readByte()];
	}

	public MinecartMetal getMetal() {
		return metal;
	}
}