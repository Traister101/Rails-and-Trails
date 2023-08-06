package mod.traister101.rnt.objects.inventory.capability;

import net.dries007.tfc.api.capability.size.CapabilityItemSize;
import net.dries007.tfc.api.capability.size.IItemSize;
import net.dries007.tfc.api.capability.size.Size;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class ChestMinecartHandler extends ItemStackHandler {

	public ChestMinecartHandler() {
		super(18);
	}

	public void dropItems(final World world, final double posX, final double posY, final double posZ) {
		// Spawn
		stacks.forEach(itemStack -> InventoryHelper.spawnItemStack(world, posX, posY, posZ, itemStack));
		stacks.clear();
	}

	@Override
	public boolean isItemValid(final int slot, @Nonnull final ItemStack stack) {
		final IItemSize size = CapabilityItemSize.getIItemSize(stack);

		if (size != null) {
			return size.getSize(stack).isSmallerThan(Size.VERY_LARGE);
		}

		return true;
	}
}