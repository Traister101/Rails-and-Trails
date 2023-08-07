package mod.traister101.rnt.objects.inventory.capability;

import mod.traister101.rnt.objects.entities.EntityMinecartBarrelRNT;
import net.dries007.tfc.api.capability.size.CapabilityItemSize;
import net.dries007.tfc.api.capability.size.IItemSize;
import net.dries007.tfc.api.capability.size.Size;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

import static net.dries007.tfc.objects.te.TEBarrel.SLOT_FLUID_CONTAINER_IN;
import static net.dries007.tfc.objects.te.TEBarrel.SLOT_ITEM;

public class BarrelMinecartHandler extends ItemStackHandler {

	private final EntityMinecartBarrelRNT barrelCart;

	public BarrelMinecartHandler(final EntityMinecartBarrelRNT barrelCart) {
		super(3);
		this.barrelCart = barrelCart;
	}

	@Nonnull
	@Override
	public ItemStack insertItem(final int slot, @Nonnull final ItemStack stack, final boolean simulate) {
		return super.insertItem(slot, stack, simulate);
	}

	@Nonnull
	@Override
	public ItemStack extractItem(final int slot, final int amount, final boolean simulate) {
		// Can't extract anything if our cart is sealed
		if ((barrelCart.isSealed())) return ItemStack.EMPTY;

		return super.extractItem(slot, amount, simulate);
	}

	@Override
	public boolean isItemValid(final int slot, @Nonnull final ItemStack stack) {
		// No items are valid if our cart is sealed
		if (barrelCart.isSealed()) return false;

		switch (slot) {
			case SLOT_FLUID_CONTAINER_IN:
				return stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
			case SLOT_ITEM:
				final IItemSize size = CapabilityItemSize.getIItemSize(stack);
				if (size != null) {
					return size.getSize(stack).isSmallerThan(Size.HUGE);
				}
				return true;
			default:
				return false;
		}
	}
}