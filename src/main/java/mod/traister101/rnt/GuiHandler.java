package mod.traister101.rnt;

import mod.traister101.rnt.client.gui.GuiBarrelMinecart;
import mod.traister101.rnt.client.gui.GuiChestMinecart;
import mod.traister101.rnt.objects.container.ContainerBarrelMinecart;
import mod.traister101.rnt.objects.container.ContainerChestMinecart;
import mod.traister101.rnt.objects.entities.EntityMinecartBarrelRNT;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {

	/**
	 * Helper for opening a GUI container from this mod for one of our entities
	 */
	public static void openGui(final World world, final EntityPlayer player, final Entity entity, final GuiType type) {
		player.openGui(RailsNTrails.getInstance(), type.ordinal(), world, entity.getEntityId(), 0, 0);
	}

	@Nullable
	@Override
	public Container getServerGuiElement(final int ID, final EntityPlayer player, final World world, final int x, final int y,
			final int z) {

		// We store entity ID in the x variable
		final Entity entity = world.getEntityByID(x);
		// If it's null we should just blow up
		assert entity != null;

		switch (GuiType.valueOf(ID)) {
			case CHEST_MINECART: {
				final IItemHandler itemHandler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
				if (itemHandler == null) return null;

				return new ContainerChestMinecart(player.inventory, itemHandler);
			}
			case BARREL_MINECART: {
				final IItemHandler itemHandler = entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
				if (itemHandler == null) return null;

				final IFluidHandler fluidHandler = entity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
				if (fluidHandler == null) return null;

				if (!(entity instanceof EntityMinecartBarrelRNT)) return null;

				return new ContainerBarrelMinecart(player.inventory, itemHandler, (EntityMinecartBarrelRNT) entity);
			}
			default:
				return null;
		}
	}

	@Nullable
	@Override
	public GuiContainer getClientGuiElement(final int ID, final EntityPlayer player, final World world, final int x, final int y,
			final int z) {
		final Container container = getServerGuiElement(ID, player, world, x, y, z);

		switch (GuiType.valueOf(ID)) {

			case CHEST_MINECART:
				return new GuiChestMinecart(container);
			case BARREL_MINECART: {
				// We store entity ID in the x variable
				final Entity entity = world.getEntityByID(x);
				assert entity != null;
				if (!(entity instanceof EntityMinecartBarrelRNT)) return null;

				return new GuiBarrelMinecart(container, (EntityMinecartBarrelRNT) entity);
			}
			default:
				return null;
		}
	}


	public enum GuiType {
		CHEST_MINECART,
		BARREL_MINECART,
		NULL;

		private static final GuiType[] values = values();

		@Nonnull
		public static GuiType valueOf(int id) {
			return id < 0 || id >= values.length ? NULL : values[id];
		}
	}
}