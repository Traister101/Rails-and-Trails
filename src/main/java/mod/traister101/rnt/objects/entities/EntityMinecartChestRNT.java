package mod.traister101.rnt.objects.entities;

import io.netty.buffer.ByteBuf;
import mod.traister101.rnt.ConfigRNT;
import mod.traister101.rnt.GuiHandler;
import mod.traister101.rnt.GuiHandler.GuiType;
import mod.traister101.rnt.objects.inventory.capability.ChestMinecartHandler;
import mod.traister101.rnt.objects.items.ItemMinecartChest;
import mod.traister101.rnt.objects.types.MinecartMetal;
import net.dries007.tfc.api.registries.TFCRegistries;
import net.dries007.tfc.api.types.Tree;
import net.dries007.tfc.objects.blocks.wood.BlockChestTFC;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public class EntityMinecartChestRNT extends EntityMinecartRNT implements IEntityAdditionalSpawnData {

	private static final Tree[] woods = TFCRegistries.TREES.getValuesCollection().toArray(new Tree[] {});

	private final ChestMinecartHandler minecartContents = new ChestMinecartHandler();
	/**
	 * When set to true, the minecart will drop all items when setDead() is called. When false (such as when travelling
	 * dimensions) it preserves its contents.
	 */
	public boolean dropContentsWhenDead = true;
	private ResourceLocation lootTable;
	private long lootTableSeed;
	private Tree wood;

	@SuppressWarnings("unused")
	public EntityMinecartChestRNT(final World worldIn) {
		super(worldIn);
	}

	public EntityMinecartChestRNT(final World worldIn, final double x, final double y, final double z,
			final MinecartMetal metal, final Tree wood) {
		super(worldIn, x, y, z, metal);
		this.wood = wood;
	}

	/**
	 * Helper constructor to take in another cart and
	 *
	 * @param otherCart Other cart to replace
	 * @param wood The wood type of our chest minecart
	 */
	public EntityMinecartChestRNT(final EntityMinecartRideableRNT otherCart, final Tree wood) {
		this(otherCart.world, otherCart.posX, otherCart.posY, otherCart.posZ, otherCart.metal, wood);
		this.motionX = otherCart.motionX;
		this.motionY = otherCart.motionY;
		this.motionZ = otherCart.motionZ;

		// Remove the other cart as we should "consume" it
		otherCart.setDropItemsWhenDead(false);
		otherCart.setDead();
	}

	public static EntityMinecartChestRNT create(final World world, final double x, final double y, final double z,
			final EnumRailDirection railDirection, final Tree wood, final MinecartMetal metal) {

		final Vec3d posOffset = getPlacementPosOffset(railDirection);

		return new EntityMinecartChestRNT(world, x + posOffset.x, y + posOffset.y, z + posOffset.z, metal, wood);
	}

	@Override
	public void killMinecart(final DamageSource source) {
		setDead();
		// Make sure we can drop items otherwise just abort
		if (!world.getGameRules().getBoolean("doEntityDrops")) return;

		// Should we drop the cart item
		if (ConfigRNT.CHEST_MINECART_CONFIG.dropAsItem) {
			final ItemStack itemStack = new ItemStack(ItemMinecartChest.get(wood, metal));

			if (hasCustomName()) itemStack.setStackDisplayName(getCustomNameTag());

			entityDropItem(itemStack, 0);
			return;
		}

		final ItemStack itemStack = new ItemStack(BlockChestTFC.getBasic(wood));

		entityDropItem(itemStack, 0);
		// Drops the rideable cart
		entityDropItem(super.getCartItem(), 0);
	}

	@Override
	public void writeSpawnData(final ByteBuf buffer) {
		super.writeSpawnData(buffer);
		// Store the wood type as a byte, there probably won't ever be more than 255 types of wood
		for (byte i = 0; i < woods.length; i++) {
			if (wood == woods[i]) {
				buffer.writeByte(i);
				return;
			}
		}
	}

	@Override
	public void readSpawnData(final ByteBuf additionalData) {
		super.readSpawnData(additionalData);
		wood = woods[additionalData.readByte()];
	}

	@Override
	public void setDead() {
		if (dropContentsWhenDead && !world.isRemote) minecartContents.dropItems(world, posX, posY, posZ);
		super.setDead();
	}

	@Override
	public void setDropItemsWhenDead(final boolean dropWhenDead) {
		dropContentsWhenDead = dropWhenDead;
	}

	@Nullable
	@Override
	public Entity changeDimension(final int dimensionIn, final ITeleporter teleporter) {
		dropContentsWhenDead = false;
		return super.changeDimension(dimensionIn, teleporter);
	}

	@Override
	public boolean hasCapability(final Capability<?> capability, final @Nullable EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T getCapability(final Capability<T> capability, final @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) minecartContents;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	protected void readEntityFromNBT(final NBTTagCompound compound) {
		super.readEntityFromNBT(compound);

		// Store the wood type as a byte, there probably won't ever be more than 255 types of wood
		wood = woods[compound.getByte("WoodType")];

		if (compound.hasKey("LootTable")) {
			lootTable = new ResourceLocation(compound.getString("LootTable"));
			lootTableSeed = compound.getLong("LootTableSeed");
			return;
		}

		minecartContents.deserializeNBT(compound.getCompoundTag("Container"));
	}

	@Override
	protected void writeEntityToNBT(final NBTTagCompound compound) {
		super.writeEntityToNBT(compound);

		// Write our wood type to NBT as a byte
		for (byte i = 0; i < woods.length; i++) {
			if (wood == woods[i]) {
				compound.setByte("WoodType", i);
				break;
			}
		}

		if (lootTable != null) {
			compound.setString("LootTable", lootTable.toString());

			if (lootTableSeed != 0L) {
				compound.setLong("LootTableSeed", lootTableSeed);
			}
			return;
		}

		compound.setTag("Container", minecartContents.serializeNBT());
	}

	@Override
	public Type getType() {
		return Type.CHEST;
	}

	@Override
	public IBlockState getDefaultDisplayTile() {
		return BlockChestTFC.getBasic(wood).getDefaultState();
	}

	@Override
	public int getDefaultDisplayTileOffset() {
		return 8;
	}

	@Override
	public boolean processInitialInteract(final EntityPlayer player, final EnumHand hand) {
		if (super.processInitialInteract(player, hand)) return true;

		if (!world.isRemote) GuiHandler.openGui(world, player, this, GuiType.CHEST_MINECART);

		return true;
	}

	public Tree getWood() {
		return wood;
	}
}