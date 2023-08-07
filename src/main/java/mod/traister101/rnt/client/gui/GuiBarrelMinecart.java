package mod.traister101.rnt.client.gui;

import mod.traister101.rnt.client.button.GuiBarrelMinecartButtonSeal;
import mod.traister101.rnt.objects.entities.EntityMinecartBarrelRNT;
import net.dries007.tfc.api.recipes.barrel.BarrelRecipe;
import net.dries007.tfc.client.FluidSpriteCache;
import net.dries007.tfc.client.button.IButtonTooltip;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.dries007.tfc.TerraFirmaCraft.MOD_ID;
import static net.dries007.tfc.client.gui.GuiBarrel.BARREL_BACKGROUND;

public class GuiBarrelMinecart extends GuiContainer {

	private final EntityMinecartBarrelRNT barrelCart;
	private final String translationKey;

	public GuiBarrelMinecart(final Container inventorySlotsIn, final EntityMinecartBarrelRNT barrelCart) {
		super(inventorySlotsIn);
		this.barrelCart = barrelCart;
		this.translationKey = barrelCart.getBarrelTranslationKey();
	}

	@Override
	public void initGui() {
		super.initGui();
		addButton(new GuiBarrelMinecartButtonSeal(barrelCart, 0, guiTop, guiLeft));
	}

	@Override
	public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void renderHoveredToolTip(int mouseX, int mouseY) {
		super.renderHoveredToolTip(mouseX, mouseY);

		final int relX = mouseX - guiLeft;
		final int relY = mouseY - guiTop;

		if (relX >= 7 && relY >= 19 && relX < 25 && relY < 71) {
			final IFluidHandler tank = barrelCart.getBarrelTank();

			final FluidStack fluid = tank.getTankProperties()[0].getContents();
			final List<String> tooltip = new ArrayList<>();

			if (fluid == null || fluid.amount == 0) {
				tooltip.add(I18n.format(MOD_ID + ".tooltip.barrel_empty"));
			} else {
				tooltip.add(fluid.getLocalizedName());
				tooltip.add(TextFormatting.GRAY + I18n.format(MOD_ID + ".tooltip.barrel_fluid_amount", fluid.amount));
			}

			drawHoveringText(tooltip, mouseX, mouseY, fontRenderer);
		}

		// Button Tooltips
		for (final GuiButton button : buttonList) {
			if (button instanceof IButtonTooltip && button.isMouseOver()) {
				final IButtonTooltip tooltip = (IButtonTooltip) button;
				if (tooltip.hasTooltip()) {
					drawHoveringText(I18n.format(tooltip.getTooltip()), mouseX, mouseY);
				}
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = I18n.format(translationKey + ".name");
		fontRenderer.drawString(name, xSize / 2 - fontRenderer.getStringWidth(name) / 2, 6, 0x404040);

		if (barrelCart.isSealed()) {
			// Draw over the input items, making them look unavailable
			final IItemHandler handler = barrelCart.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			if (handler != null) {
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				for (int slotId = 0; slotId < handler.getSlots(); slotId++) {
					drawSlotOverlay(inventorySlots.getSlot(slotId));
				}
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			}

			// Draw the text displaying both the seal date, and the recipe name
			boolean isLong = false;
			final BarrelRecipe recipe = barrelCart.getRecipe();
			if (recipe != null) {
				final String resultName = recipe.getResultName();
				final int recipeWidth = fontRenderer.getStringWidth(resultName);
				if (recipeWidth > 80) isLong = true;
				fontRenderer.drawString(resultName, xSize / 2 - (isLong ? recipeWidth / 2 - 6 : 28), isLong ? 73 : 61, 0x404040);
			}
			fontRenderer.drawString(barrelCart.getSealedDate(),
					xSize / 2 - (isLong ? 28 : fontRenderer.getStringWidth(barrelCart.getSealedDate()) / 2),
					isLong ? 19 : 73, 0x404040);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
		GlStateManager.color(1, 1, 1, 1);
		mc.getTextureManager().bindTexture(BARREL_BACKGROUND);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if (Helpers.isJEIEnabled()) {
			drawTexturedModalRect(guiLeft + 92, guiTop + 21, 227, 0, 9, 14);
		}

		final IFluidHandler tank = barrelCart.getBarrelTank();

		final IFluidTankProperties tankProperties = tank.getTankProperties()[0];
		final FluidStack fluidStack = tankProperties.getContents();

		if (fluidStack != null) {
			int fillHeightPixels = (50 * fluidStack.amount / tankProperties.getCapacity());

			if (fillHeightPixels > 0) {
				final Fluid fluid = fluidStack.getFluid();
				final TextureAtlasSprite fluidSprite = FluidSpriteCache.getStillSprite(fluid);

				final int positionX = guiLeft + 8;
				int positionY = guiTop + 54;

				Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				final BufferBuilder buffer = Tessellator.getInstance().getBuffer();

				GlStateManager.enableAlpha();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
						GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

				final int color = fluid.getColor();

				final float r = ((color >> 16) & 0xFF) / 255f;
				final float g = ((color >> 8) & 0xFF) / 255f;
				final float b = (color & 0xFF) / 255f;
				final float a = ((color >> 24) & 0xFF) / 255f;

				GlStateManager.color(r, g, b, a);

				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

				while (fillHeightPixels > 15) {
					buffer.pos(positionX, positionY, 0).tex(fluidSprite.getMinU(), fluidSprite.getMinV()).endVertex();
					buffer.pos(positionX, positionY + 16, 0).tex(fluidSprite.getMinU(), fluidSprite.getMaxV()).endVertex();
					buffer.pos(positionX + 16, positionY + 16, 0).tex(fluidSprite.getMaxU(), fluidSprite.getMaxV()).endVertex();
					buffer.pos(positionX + 16, positionY, 0).tex(fluidSprite.getMaxU(), fluidSprite.getMinV()).endVertex();

					fillHeightPixels -= 16;
					positionY -= 16;
				}

				if (fillHeightPixels > 0) {
					int blank = 16 - fillHeightPixels;
					positionY += blank;
					buffer.pos(positionX, positionY, 0).tex(fluidSprite.getMinU(), fluidSprite.getInterpolatedV(blank)).endVertex();
					buffer.pos(positionX, positionY + fillHeightPixels, 0).tex(fluidSprite.getMinU(), fluidSprite.getMaxV()).endVertex();
					buffer.pos(positionX + 16, positionY + fillHeightPixels, 0)
							.tex(fluidSprite.getMaxU(), fluidSprite.getMaxV())
							.endVertex();
					buffer.pos(positionX + 16, positionY, 0).tex(fluidSprite.getMaxU(), fluidSprite.getInterpolatedV(blank)).endVertex();
				}

				Tessellator.getInstance().draw();

				Minecraft.getMinecraft().renderEngine.bindTexture(BARREL_BACKGROUND);
				GlStateManager.color(1, 1, 1, 1);
			}
		}

		drawTexturedModalRect(guiLeft + 7, guiTop + 19, 176, 0, 18, 52);
	}

	protected void drawSlotOverlay(Slot slot) {
		final int xPos = slot.xPos - 1;
		final int yPos = slot.yPos - 1;
		drawGradientRect(xPos, yPos, xPos + 18, yPos + 18, 1979711487, 1979711487);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
//		TerraFirmaCraft.getNetwork().sendToServer(new PacketGuiButton(button.id));
//		super.actionPerformed(button);
	}
}