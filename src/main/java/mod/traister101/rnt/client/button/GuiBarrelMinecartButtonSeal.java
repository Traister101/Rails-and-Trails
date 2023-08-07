package mod.traister101.rnt.client.button;

import mod.traister101.rnt.objects.entities.EntityMinecartBarrelRNT;
import net.dries007.tfc.client.button.GuiButtonTFC;
import net.dries007.tfc.client.button.IButtonTooltip;
import net.dries007.tfc.client.gui.GuiBarrel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nonnull;

import static net.dries007.tfc.TerraFirmaCraft.MOD_ID;

public class GuiBarrelMinecartButtonSeal extends GuiButtonTFC implements IButtonTooltip {

	private final EntityMinecartBarrelRNT barrelCart;

	public GuiBarrelMinecartButtonSeal(final EntityMinecartBarrelRNT barrelCart, final int buttonID, final int guiTop, final int guiLeft) {
		super(buttonID, guiLeft + 123, guiTop + 35, 20, 20, "");
		this.barrelCart = barrelCart;
	}

	@Override
	public String getTooltip() {
		return MOD_ID + ".tooltip." + (barrelCart.isSealed() ? "barrel_unseal" : "barrel_seal");
	}

	@Override
	public boolean hasTooltip() {
		return true;
	}

	@Override
	public void drawButton(@Nonnull final Minecraft mc, final int mouseX, final int mouseY, final float partialTicks) {
		if (visible) {
			GlStateManager.color(1, 1, 1, 1);
			mc.getTextureManager().bindTexture(GuiBarrel.BARREL_BACKGROUND);
			hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			if (barrelCart.isSealed()) {
				drawModalRectWithCustomSizedTexture(x, y, 236, 0, 20, 20, 256, 256);
			} else {
				drawModalRectWithCustomSizedTexture(x, y, 236, 20, 20, 20, 256, 256);
			}

			mouseDragged(mc, mouseX, mouseY);
		}
	}
}
