package mod.traister101.rnt.network;

import io.netty.buffer.ByteBuf;
import mod.traister101.rnt.RailsNTrails;
import mod.traister101.rnt.objects.entities.EntityMinecartBarrelRNT;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateMinecartBarrel implements IMessage {

	private int cartID;
	private boolean isSealed;
	private ItemStack containedItem;
	private NBTTagCompound tankData;

	public UpdateMinecartBarrel() {
	}


	public UpdateMinecartBarrel(final EntityMinecartBarrelRNT barrelCart) {
		this.cartID = barrelCart.getEntityId();
		this.tankData = barrelCart.getBarrelTank().writeToNBT(new NBTTagCompound());
		this.containedItem = barrelCart.getShownItem();
		this.isSealed = barrelCart.isSealed();
	}

	@Override
	public void fromBytes(final ByteBuf buf) {
		cartID = buf.readInt();
		isSealed = buf.readBoolean();
		tankData = ByteBufUtils.readTag(buf);
		// Can't use the item stack helper it doesn't write the forge capabilities which is
		// especially bad with TFC as that's how decay is stored
		//noinspection DataFlowIssue
		containedItem = new ItemStack(ByteBufUtils.readTag(buf));
	}

	@Override
	public void toBytes(final ByteBuf buf) {
		buf.writeInt(cartID);
		buf.writeBoolean(isSealed);
		ByteBufUtils.writeTag(buf, tankData);
		// Can't use the item stack helper it doesn't write the forge capabilities which is
		// especially bad with TFC as that's how decay is stored
		ByteBufUtils.writeTag(buf, containedItem.serializeNBT());
	}

	public static class Handler implements IMessageHandler<UpdateMinecartBarrel, IMessage> {

		@Override
		public IMessage onMessage(final UpdateMinecartBarrel message, final MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
				final Entity entity = Minecraft.getMinecraft().world.getEntityByID(message.cartID);

				if (!(entity instanceof EntityMinecartBarrelRNT)) {
					RailsNTrails.getLog().debug("Attempted to update an entity which isn't a Barrel Minecart!");
					return;
				}

				final EntityMinecartBarrelRNT cart = (EntityMinecartBarrelRNT) entity;
				cart.getBarrelTank().readFromNBT(message.tankData);
				cart.setShownItem(message.containedItem);
				// If the seal states don't match toggle it
				if (message.isSealed != cart.isSealed()) cart.toggleSeal();
			});

			return null;
		}
	}
}