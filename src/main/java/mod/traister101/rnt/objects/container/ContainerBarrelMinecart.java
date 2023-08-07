package mod.traister101.rnt.objects.container;

import mod.traister101.rnt.objects.entities.EntityMinecartBarrelRNT;
import net.dries007.tfc.objects.container.IButtonHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nullable;

import static net.dries007.tfc.objects.te.TEBarrel.*;

public class ContainerBarrelMinecart extends Container implements IButtonHandler {

	private final EntityMinecartBarrelRNT barrelCart;

	public ContainerBarrelMinecart(final InventoryPlayer playerInventory, final IItemHandler itemHandler,
			final EntityMinecartBarrelRNT barrelCart) {
		this.barrelCart = barrelCart;

		addSlotToContainer(new SlotItemHandler(itemHandler, SLOT_FLUID_CONTAINER_IN, 35, 20));
		addSlotToContainer(new SlotItemHandler(itemHandler, SLOT_FLUID_CONTAINER_OUT, 35, 54));
		addSlotToContainer(new SlotItemHandler(itemHandler, SLOT_ITEM, 89, 37));

		// Adding player slots
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		// Adding hot bar slots
		for (int i = 0; i < 9; ++i) {
			addSlotToContainer(new Slot(playerInventory, i, 8 + i * 18, 142));
		}
	}

	@Override
	public ItemStack transferStackInSlot(final EntityPlayer player, final int index) {
		final Slot slot = inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			final ItemStack slotStack = slot.getStack();
			final int slotCount = inventorySlots.size() - player.inventory.mainInventory.size();
			if (index < slotCount) {
				if (!mergeItemStack(slotStack, slotCount, inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!mergeItemStack(slotStack, 0, slotCount, false)) {
				return ItemStack.EMPTY;
			}

			if (slotStack.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canInteractWith(final EntityPlayer player) {
		return true;
	}

	@Override
	public void onButtonPress(final int buttonID, @Nullable final NBTTagCompound nbtTagCompound) {
		// buttonID should always be 0 since we only add one button
		if (!barrelCart.world.isRemote) barrelCart.toggleSeal();
	}
}
