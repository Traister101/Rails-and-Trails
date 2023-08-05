package mod.traister101.rnt.objects.entities;

import net.dries007.tfc.api.types.Tree;
import net.dries007.tfc.objects.blocks.wood.BlockChestTFC;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityMinecartChestRNT extends EntityMinecartRNT {

	private Tree wood;

	@SuppressWarnings("unused")
	public EntityMinecartChestRNT(final World worldIn) {
		super(worldIn);
	}

	public EntityMinecartChestRNT(final World worldIn, final double x, final double y, final double z, final Tree wood) {
		super(worldIn, x, y, z);
		setDisplayTile(BlockChestTFC.getBasic(wood).getDefaultState());
		this.wood = wood;
	}

	/**
	 * Helper constructor to take in another cart and
	 *
	 * @param otherCart Other cart to replace
	 * @param wood The wood type of our chest minecart
	 */
	public EntityMinecartChestRNT(final EntityMinecartRideableRNT otherCart, final Tree wood) {
		this(otherCart.world, otherCart.posX, otherCart.posY, otherCart.posZ, wood);
		this.motionX = otherCart.motionX;
		this.motionY = otherCart.motionY;
		this.motionZ = otherCart.motionZ;

		otherCart.setDropItemsWhenDead(false);
		otherCart.setDead();
	}

	public static EntityMinecartChestRNT create(final World world, final double x, final double y, final double z,
			final EnumRailDirection railDirection, final Tree wood) {

		final Vec3d posOffset = getPlacementPosOffset(railDirection);

		return new EntityMinecartChestRNT(world, x + posOffset.x, y + posOffset.y, z + posOffset.z, wood);
	}

	@Override
	public void killMinecart(final DamageSource source) {
		super.killMinecart(source);

		if (world.getGameRules().getBoolean("doEntityDrops")) {
			final ItemStack itemStack = new ItemStack(BlockChestTFC.getBasic(wood));

			if (hasCustomName()) itemStack.setStackDisplayName(getCustomNameTag());

			entityDropItem(itemStack, 0);
		}
	}

	@Override
	public Type getType() {
		return Type.CHEST;
	}

	@Override
	public int getDefaultDisplayTileOffset() {
		return 8;
	}
}