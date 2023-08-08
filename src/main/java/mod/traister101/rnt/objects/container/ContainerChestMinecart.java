package mod.traister101.rnt.objects.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerChestMinecart extends Container {

	public ContainerChestMinecart(final InventoryPlayer playerInventory, final IItemHandler itemHandler) {
		final int numRows = itemHandler.getSlots() / 9;
		final int i = (numRows - 4) * 18;

		// Adding chest slots
		for (int j = 0; j < numRows; j++) {
			for (int k = 0; k < 9; k++) {
				addSlotToContainer(new SlotItemHandler(itemHandler, k + j * 9, 8 + k * 18, 18 + j * 18));
			}
		}

		// Adding player slots
		for (int j = 0; j < 3; ++j) {
			for (int k = 0; k < 9; ++k) {
				addSlotToContainer(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 103 + j * 18 + i));
			}
		}

		// Adding hot bar slots
		for (int j = 0; j < 9; ++j) {
			addSlotToContainer(new Slot(playerInventory, j, 8 + j * 18, 161 + i));
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
	public boolean canInteractWith(final EntityPlayer playerIn) {
		return true;
	}
}