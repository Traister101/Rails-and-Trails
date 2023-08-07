package mod.traister101.rnt.client.renderer.entity;

import mod.traister101.rnt.objects.entities.EntityMinecartBarrelRNT;
import net.dries007.tfc.client.FluidSpriteCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class RenderMinecartBarrelRNT extends RenderMinecartRNT<EntityMinecartBarrelRNT> {

	public RenderMinecartBarrelRNT(final RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected void renderCartContents(final EntityMinecartBarrelRNT cart, final float partialTicks, final IBlockState blockState) {
		// Render our barrel
		super.renderCartContents(cart, partialTicks, blockState);

		final IFluidHandler fluidHandler = cart.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
		final IItemHandler itemHandler = cart.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if (fluidHandler != null && itemHandler != null) {
			final IFluidTankProperties properties = fluidHandler.getTankProperties()[0];
			final FluidStack fluidStack = properties.getContents();
			final ItemStack stack = itemHandler.getStackInSlot(2);
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, -1);
			if (!stack.isEmpty()) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(0.5, 0.15625, 0.5);
				GlStateManager.scale(0.5, 0.5, 0.5);
				GlStateManager.rotate(90, 1, 0, 0);
				Minecraft.getMinecraft().getRenderItem().renderItem(stack, TransformType.FIXED);
				GlStateManager.popMatrix();
			}

			if (fluidStack != null) {
				final Fluid fluid = fluidStack.getFluid();
				final TextureAtlasSprite sprite = FluidSpriteCache.getStillSprite(fluid);
				GlStateManager.enableAlpha();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE,
						DestFactor.ZERO);

				{
					final int color = fluid.getColor();
					final float r = (float) (color >> 16 & 255) / 255;
					final float g = (float) (color >> 8 & 255) / 255;
					final float b = (float) (color & 255) / 255;
					final float a = (float) (color >> 24 & 255) / 255;
					GlStateManager.color(r, g, b, a);
				}
				renderManager.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				final BufferBuilder buffer = Tessellator.getInstance().getBuffer();
				buffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
				final FluidStack content = properties.getContents();

				if (content == null) {
					return;
				}

				final double height = 0.140625 + 0.734375 * (double) content.amount / (double) properties.getCapacity();
				buffer.pos(0.1875, height, 0.1875)
						.tex(sprite.getInterpolatedU(3.0), sprite.getInterpolatedV(3.0))
						.normal(0, 0, 1)
						.endVertex();
				buffer.pos(0.1875, height, 0.8125)
						.tex(sprite.getInterpolatedU(3.0), sprite.getInterpolatedV(13.0))
						.normal(0.0F, 0.0F, 1.0F)
						.endVertex();
				buffer.pos(0.8125, height, 0.8125)
						.tex(sprite.getInterpolatedU(13.0), sprite.getInterpolatedV(13.0))
						.normal(0.0F, 0.0F, 1.0F)
						.endVertex();
				buffer.pos(0.8125, height, 0.1875)
						.tex(sprite.getInterpolatedU(13.0), sprite.getInterpolatedV(3.0))
						.normal(0.0F, 0.0F, 1.0F)
						.endVertex();
				Tessellator.getInstance().draw();
			}

			GlStateManager.popMatrix();
		}
	}
}
