package mod.traister101.rnt.objects.entities;

import io.netty.buffer.ByteBuf;
import mod.traister101.rnt.GuiHandler;
import mod.traister101.rnt.GuiHandler.GuiType;
import mod.traister101.rnt.objects.fluids.capability.BarrelMinecartFluidTank;
import mod.traister101.rnt.objects.inventory.capability.BarrelMinecartHandler;
import net.dries007.tfc.ConfigTFC;
import net.dries007.tfc.api.recipes.barrel.BarrelRecipe;
import net.dries007.tfc.objects.blocks.BlocksTFC;
import net.dries007.tfc.objects.blocks.wood.BlockBarrel;
import net.dries007.tfc.objects.items.itemblock.ItemBlockBarrel.ItemBarrelFluidHandler;
import net.dries007.tfc.util.FluidTransferHelper;
import net.dries007.tfc.util.calendar.CalendarTFC;
import net.dries007.tfc.util.calendar.ICalendarFormatted;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static net.dries007.tfc.objects.te.TEBarrel.*;

public class EntityMinecartBarrelRNT extends EntityMinecartRNT {

	protected static final List<BlockBarrel> barrels = new ArrayList<>();

	static {
		BlocksTFC.getAllBarrelItemBlocks().forEach(itemBlockBarrel -> barrels.add((BlockBarrel) itemBlockBarrel.getBlock()));
	}

	protected final ItemStackHandler inventory = new BarrelMinecartHandler(this);
	protected final FluidTank tank = new BarrelMinecartFluidTank(this);
	protected final Queue<ItemStack> surplus = new LinkedList<>(); // Surplus items from a recipe with output > stackSize
	protected BlockBarrel heldBarrel;
	protected IBlockState blockState;
	protected boolean sealed = false;
	protected long sealedTick, sealedCalendarTick;
	// Last player tick this barrel was ticked (for purposes of catching up)
	protected long lastPlayerTick;
	protected int tickCounter;
	protected boolean checkInstantRecipe = false;
	private BarrelRecipe recipe;
	private boolean needsClientUpdate;
	@SideOnly(Side.CLIENT)
	private String translationKey;

	@SuppressWarnings("unused")
	public EntityMinecartBarrelRNT(final World worldIn) {
		super(worldIn);
	}

	/**
	 * Helper constructor to take in another cart and an item stack holding a barrel
	 *
	 * @param otherCart Other cart to replace
	 * @param barrelItemStack Our barrel item stack
	 */
	public EntityMinecartBarrelRNT(final EntityMinecartRideableRNT otherCart, final ItemStack barrelItemStack) {
		super(otherCart.world, otherCart.posX, otherCart.posY, otherCart.posZ, otherCart.metal);
		this.motionX = otherCart.motionX;
		this.motionY = otherCart.motionY;
		this.motionZ = otherCart.motionZ;
		this.heldBarrel = (BlockBarrel) ((ItemBlock) barrelItemStack.getItem()).getBlock();
		this.blockState = heldBarrel.getDefaultState();

		loadFromItemStack(barrelItemStack);

		// Consume other cart
		otherCart.setDropItemsWhenDead(false);
		otherCart.setDead();
	}

	@Override
	public void killMinecart(final DamageSource source) {
		setDead();
		// Make sure we can drop items otherwise just abort
		if (!world.getGameRules().getBoolean("doEntityDrops")) return;

		super.killMinecart(source);
		final ItemStack itemStack = new ItemStack(heldBarrel);
		saveToItemStack(itemStack);
		entityDropItem(itemStack, 0);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		checkForCalendarUpdate();

		if (world.isRemote) {
			return;
		}

		if (needsClientUpdate) {
			needsClientUpdate = false;
			// TODO send update packet
		}

		final BlockPos blockPos = new BlockPos(posX, posY, posZ);

		tickCounter++;
		if (tickCounter == 10) {
			tickCounter = 0;

			final ItemStack fluidContainerIn = inventory.getStackInSlot(SLOT_FLUID_CONTAINER_IN);
			FluidActionResult result = FluidTransferHelper.emptyContainerIntoTank(fluidContainerIn, tank, inventory,
					SLOT_FLUID_CONTAINER_OUT, ConfigTFC.Devices.BARREL.tank, world, blockPos);

			if (!result.isSuccess()) {
				result = FluidTransferHelper.fillContainerFromTank(fluidContainerIn, tank, inventory, SLOT_FLUID_CONTAINER_OUT,
						ConfigTFC.Devices.BARREL.tank, world, blockPos);
			}

			if (result.isSuccess()) {
				inventory.setStackInSlot(SLOT_FLUID_CONTAINER_IN, result.getResult());
			}

			final Fluid freshWater = FluidRegistry.getFluid("fresh_water");

			if (!sealed && world.isRainingAt(blockPos.up()) && (tank.getFluid() == null || tank.getFluid().getFluid() == freshWater)) {
				tank.fill(new FluidStack(freshWater, 10), true);
			}

			if (inventory.getStackInSlot(SLOT_ITEM) == ItemStack.EMPTY && !surplus.isEmpty()) {
				inventory.setStackInSlot(SLOT_ITEM, surplus.poll());
			}
		}

		// Check if recipe is complete (sealed recipes only)
		if (recipe != null && sealed) {
			final int durationSealed = (int) (CalendarTFC.PLAYER_TIME.getTicks() - sealedTick);

			if (recipe.getDuration() > 0 && durationSealed > recipe.getDuration()) doSealedRecipe();
		}

		if (checkInstantRecipe) {
			final ItemStack inputStack = inventory.getStackInSlot(SLOT_ITEM);
			final FluidStack inputFluid = tank.getFluid();
			final BarrelRecipe instantRecipe = BarrelRecipe.getInstant(inputStack, inputFluid);
			if (instantRecipe != null && inputFluid != null && instantRecipe.isValidInputInstant(inputStack, inputFluid)) {
				tank.setFluid(instantRecipe.getOutputFluid(inputFluid, inputStack));
				final List<ItemStack> output = instantRecipe.getOutputItem(inputFluid, inputStack);
				final ItemStack first = output.get(0);
				output.remove(0);
				inventory.setStackInSlot(SLOT_ITEM, first);
				surplus.addAll(output);
				instantRecipe.onRecipeComplete(world, blockPos);
				markForSync();
			} else {
				checkInstantRecipe = false;
			}
		}
	}

	@Override
	protected void readEntityFromNBT(final NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		heldBarrel = (BlockBarrel) Block.getBlockFromName(compound.getString("HeldBarrel"));
		inventory.deserializeNBT(compound.getCompoundTag("Container"));
		tank.readFromNBT(compound.getCompoundTag("Tank"));
		sealed = compound.getBoolean("Sealed");
		blockState = heldBarrel.getDefaultState();
	}

	@Override
	protected void writeEntityToNBT(final NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setString("HeldBarrel", Block.REGISTRY.getNameForObject(heldBarrel).toString());
		compound.setTag("Container", inventory.serializeNBT());
		compound.setTag("Tank", tank.writeToNBT(new NBTTagCompound()));
		compound.setBoolean("Sealed", sealed);
	}

	@Override
	public void writeSpawnData(final ByteBuf buffer) {
		super.writeSpawnData(buffer);
		buffer.writeByte(barrels.indexOf(heldBarrel));
		buffer.writeBoolean(sealed);
		ByteBufUtils.writeTag(buffer, tank.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readSpawnData(final ByteBuf additionalData) {
		super.readSpawnData(additionalData);
		heldBarrel = barrels.get(additionalData.readByte());
		translationKey = heldBarrel.getTranslationKey();
		sealed = additionalData.readBoolean();
		blockState = heldBarrel.getDefaultState().withProperty(BlockBarrel.SEALED, sealed);
		tank.readFromNBT(ByteBufUtils.readTag(additionalData));
	}

	private void doSealedRecipe() {
		final ItemStack inputStack = inventory.getStackInSlot(SLOT_ITEM);
		final FluidStack inputFluid = tank.getFluid();
		if (recipe.isValidInput(inputFluid, inputStack)) {
			tank.setFluid(recipe.getOutputFluid(inputFluid, inputStack));
			final List<ItemStack> output = recipe.getOutputItem(inputFluid, inputStack);
			final ItemStack first = output.get(0);
			output.remove(0);
			inventory.setStackInSlot(SLOT_ITEM, first);
			surplus.addAll(output);
			markForSync();
			onSeal(); //run the sealed check again in case we have a new valid recipe.
		} else {
			recipe = null;
		}
	}

	private void markForSync() {
		needsClientUpdate = true;
	}

	private void checkForCalendarUpdate() {
		if (world != null && !world.isRemote) {
			final long playerTick = CalendarTFC.PLAYER_TIME.getTicks();
			final long tickDelta = playerTick - lastPlayerTick;
			// Expect 1 tick
			if (tickDelta != 1) {
				onCalendarUpdate(tickDelta - 1);
			}
			lastPlayerTick = playerTick;
		}
	}

	public void onCalendarUpdate(long deltaPlayerTicks) {
		while (deltaPlayerTicks > 0) {
			if (recipe == null) return;
			deltaPlayerTicks = 0;

			if (!sealed || recipe.getDuration() <= 0) continue;


			final long tickFinish = sealedTick + recipe.getDuration();
			if (tickFinish <= CalendarTFC.PLAYER_TIME.getTicks()) {
				// Mark to run this transaction again in case this recipe produces valid output
				// for another which could potentially finish in this time period.
				deltaPlayerTicks = 1;
				final long offset = tickFinish - CalendarTFC.PLAYER_TIME.getTicks();

				CalendarTFC.runTransaction(offset, offset, this::doSealedRecipe);
			}
		}
	}

	/**
	 * Saves our data to an item stack (to drop in the world)
	 *
	 * @param itemStack Item Stack we should save to
	 */
	public void saveToItemStack(final ItemStack itemStack) {
		final IFluidHandler barrelCap = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
		if (barrelCap instanceof ItemBarrelFluidHandler) {
			NBTTagCompound inventoryTag = null;
			// Check if inventory has contents
			for (int i = 0; i < inventory.getSlots(); i++) {
				if (!inventory.getStackInSlot(i).isEmpty()) {
					inventoryTag = inventory.serializeNBT();
					break;
				}
			}

			NBTTagList surplusTag = null;
			// Check if there's remaining surplus from recipe
			if (!surplus.isEmpty()) {
				surplusTag = new NBTTagList();
				for (final ItemStack surplusStack : surplus) {
					surplusTag.appendTag(surplusStack.serializeNBT());
				}
			}

			final FluidStack tankFluid = tank.getFluid();
			if (tankFluid != null || inventoryTag != null || surplusTag != null) {
				((ItemBarrelFluidHandler) barrelCap).setBarrelContents(tankFluid, inventoryTag, surplusTag, sealedTick, sealedCalendarTick);
			}
		}
	}

	/**
	 * Initializes our entity from the item stacks fluid handler
	 *
	 * @param itemStack Item Stack we should load from
	 */
	private void loadFromItemStack(final ItemStack itemStack) {
		final IFluidHandler barrelCap = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
		if (barrelCap instanceof ItemBarrelFluidHandler) {
			final ItemBarrelFluidHandler barrelHandler = (ItemBarrelFluidHandler) barrelCap;
			final NBTTagCompound contents = barrelHandler.getBarrelContents();
			if (contents != null) {
				inventory.deserializeNBT(contents.getCompoundTag("inventory"));
				surplus.clear();

				final NBTTagList surplusItems = contents.getTagList("surplus", NBT.TAG_COMPOUND);
				if (!surplusItems.isEmpty()) {
					for (int i = 0; i < surplusItems.tagCount(); i++) {
						surplus.add(new ItemStack(surplusItems.getCompoundTagAt(i)));
					}
				}

				sealedTick = contents.getLong("sealedTick");
				sealedCalendarTick = contents.getLong("sealedCalendarTick");
				tank.fill(barrelHandler.getFluid(), true);
				sealed = true;
				blockState.withProperty(BlockBarrel.SEALED, true);
				recipe = BarrelRecipe.get(inventory.getStackInSlot(SLOT_ITEM), tank.getFluid());
				markForSync();
			}
		}
	}

	@Override
	public boolean hasCapability(final Capability<?> capability, @Nullable final EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(final Capability<T> capability, @Nullable final EnumFacing facing) {
		// Item handler
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T) inventory;

		// Fluid handler
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return (T) tank;

		return super.getCapability(capability, facing);
	}

	@Override
	public Type getType() {
		return Type.CHEST;
	}

	@Override
	public IBlockState getDefaultDisplayTile() {
		return blockState;
	}

	@Override
	public boolean processInitialInteract(final EntityPlayer player, final EnumHand hand) {
		if (super.processInitialInteract(player, hand)) return true;

		final ItemStack heldStack = player.getHeldItem(EnumHand.MAIN_HAND);

		if (player.isSneaking()) {
			// The held item doesn't have fluid capability so we should toggle the seal
			if (!heldStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
				toggleSeal();
				world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1, 0.85F);
				player.swingArm(EnumHand.MAIN_HAND);
				return true;
			}
		}

		// Empty or our barrel is sealed we should just open GUI
		if (heldStack.isEmpty() || sealed) {
			GuiHandler.openGui(world, player, this, GuiType.BARREL_MINECART);
			return true;
		}

		// If it doesn't have the fluid capability open GUI
		if (!heldStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
			GuiHandler.openGui(world, player, this, GuiType.BARREL_MINECART);
			return true;
		}

		final IFluidHandlerItem fluidHandler = heldStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
		// We checked if it has a handler already so this should never be null
		assert fluidHandler != null;

		FluidUtil.interactWithFluidHandler(player, hand, tank);
		markForSync();

		return true;
	}

	public void toggleSeal() {
		if (sealed) onUnSealed();
		else onSeal();
	}

	public void onSeal() {
		sealed = true;
		blockState = blockState.withProperty(BlockBarrel.SEALED, true);

		if (!world.isRemote) {
			for (final int slotIndex : new int[] {0, 1}) {
				InventoryHelper.spawnItemStack(world, posX, posY, posZ, inventory.getStackInSlot(slotIndex));
				inventory.setStackInSlot(slotIndex, ItemStack.EMPTY);
			}
		}

		sealedTick = CalendarTFC.PLAYER_TIME.getTicks();
		sealedCalendarTick = CalendarTFC.CALENDAR_TIME.getTicks();
		recipe = BarrelRecipe.get(inventory.getStackInSlot(2), tank.getFluid());
		if (recipe != null) recipe.onBarrelSealed(tank.getFluid(), inventory.getStackInSlot(2));

		markForSync();
	}

	private void onUnSealed() {
		sealedTick = sealedCalendarTick = 0;
		sealed = false;
		blockState = blockState.withProperty(BlockBarrel.SEALED, false);

		if (recipe != null) {
			final ItemStack inputStack = inventory.getStackInSlot(2);
			final FluidStack inputFluid = tank.getFluid();
			if (recipe.isValidInput(inputFluid, inputStack)) {
				tank.setFluid(recipe.getOutputFluidOnUnseal(inputFluid, inputStack));
				final List<ItemStack> output = recipe.getOutputItemOnUnseal(inputFluid, inputStack);
				final ItemStack first = output.get(0);
				output.remove(0);
				inventory.setStackInSlot(2, first);
				surplus.addAll(output);
			}
		}

		recipe = null;
		markForSync();
	}

	public boolean isSealed() {
		return sealed;
	}

	@Nonnull
	public String getSealedDate() {
		return ICalendarFormatted.getTimeAndDate(sealedCalendarTick, CalendarTFC.CALENDAR_TIME.getDaysInMonth());
	}

	@Nullable
	public BarrelRecipe getRecipe() {
		return recipe;
	}

	public IFluidHandler getBarrelTank() {
		return tank;
	}

	@SideOnly(Side.CLIENT)
	public String getBarrelTranslationKey() {
		return translationKey;
	}
}