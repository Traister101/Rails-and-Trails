package mod.traister101.rnt.objects.entities;

import mod.traister101.rnt.objects.types.MinecartMetal;
import net.dries007.tfc.objects.blocks.wood.BlockChestTFC;
import net.dries007.tfc.objects.items.itemblock.ItemBlockBarrel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityMinecartRideableRNT extends EntityMinecartRNT {

	@SuppressWarnings("unused")
	public EntityMinecartRideableRNT(final World worldIn) {
		super(worldIn);
	}

	public EntityMinecartRideableRNT(final World worldIn, final double x, final double y, final double z, final MinecartMetal metal) {
		super(worldIn, x, y, z, metal);
	}

	/**
	 * Helper that handles the slightly unintuitive minecart placement on rails
	 *
	 * @param world World the entity is given
	 * @param x X position of the minecart
	 * @param y Y position of the minecart
	 * @param z Z position of the minecart
	 * @param railDirection Enum for the rail we are spawning the minecart onto
	 * @param metal Our metal type
	 *
	 * @return A new minecart entity
	 */
	public static EntityMinecartRideableRNT create(final World world, final double x, final double y, final double z,
			final EnumRailDirection railDirection, final MinecartMetal metal) {

		final Vec3d posOffset = getPlacementPosOffset(railDirection);

		return new EntityMinecartRideableRNT(world, x + posOffset.x, y + posOffset.y, z + posOffset.z, metal);
	}

	@Override
	public String getName() {
		if (hasCustomName()) return getCustomNameTag();

		return I18n.format("entity." + EntitiesRNT.MINECART_RIDEABLE + "." + metal + ".name");
	}

	@Override
	public Type getType() {
		return Type.RIDEABLE;
	}

	@Override
	public boolean processInitialInteract(final EntityPlayer player, final EnumHand hand) {
		if (super.processInitialInteract(player, hand)) return true;

		// Must not be sneaking to ride
		if (!player.isSneaking()) {
			// Ensure no riders
			if (!isBeingRidden()) {
				if (!world.isRemote) {
					player.startRiding(this);
				}
				return true;
			}
		}

		final ItemStack heldStack = player.getHeldItem(EnumHand.MAIN_HAND);

		// The held stack isn't a block
		if (!(heldStack.getItem() instanceof ItemBlock)) return false;

		final ItemBlock itemBlock = (ItemBlock) heldStack.getItem();
		final Block block = itemBlock.getBlock();

		if (block instanceof BlockChestTFC) {
			// Wrong kind of chest
			if (((BlockChestTFC) block).chestType == BlockChest.Type.TRAP) return false;
			// Spawn chest minecart consuming the minecart we pass in
			if (!world.isRemote) world.spawnEntity(new EntityMinecartChestRNT(this, ((BlockChestTFC) block).wood));

			heldStack.shrink(1);
			return true;
		}

		if (itemBlock instanceof ItemBlockBarrel) {
			if (!world.isRemote) world.spawnEntity(new EntityMinecartBarrelRNT(this, heldStack));

			heldStack.shrink(1);
			return true;
		}

		return true;
	}
}