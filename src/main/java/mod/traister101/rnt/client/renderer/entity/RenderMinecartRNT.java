package mod.traister101.rnt.client.renderer.entity;

import mod.traister101.rnt.objects.entities.EntityMinecartRNT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelMinecart;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class RenderMinecartRNT<T extends EntityMinecartRNT> extends Render<T> {

	private static final ResourceLocation MINECART_TEXTURES = new ResourceLocation("textures/entity/minecart.png");
	/**
	 * instance of ModelMinecart for rendering
	 */
	protected ModelBase modelMinecart = new ModelMinecart();

	@SuppressWarnings("unused")
	public RenderMinecartRNT(final RenderManager renderManager) {
		super(renderManager);
		shadowSize = 0.5F;
	}

	private void temp(final T entity, final double x, final double y, final double z, float entityYaw, final float partialTicks) {
		GlStateManager.pushMatrix();
		bindEntityTexture(entity);

		final Vec3d entityPos = entity.getPos(entity.posX, entity.posY, entity.posZ);

		final float entityPitch;
		if (entityPos != null) {
			final Vec3d temp = entityPos.normalize();
			entityYaw = (float) (Math.atan2(temp.z, temp.x) * 180 / Math.PI);
			entityPitch = (float) (Math.atan(temp.y) * 73);
		} else {
			entityPitch = entity.rotationPitch;
		}

		GlStateManager.translate(x, y + 0.375, z);
		GlStateManager.rotate(180 - entityYaw, 0, 1, 0);
		GlStateManager.rotate(-entityPitch, 0, 0, 1);
		{
			float damageAmount = entity.getDamage() - partialTicks;

			if (damageAmount < 0) {
				damageAmount = 0;
			}

			final float rollAngle = entity.getRollingAmplitude() - partialTicks;
			if (rollAngle > 0) {
				GlStateManager.rotate(MathHelper.sin(rollAngle) * rollAngle * damageAmount / 10 * entity.getRollingDirection(), 1, 0, 0);
			}
		}

		if (renderOutlines) {
			GlStateManager.enableColorMaterial();
			GlStateManager.enableOutlineMode(getTeamColor(entity));
		}

		final IBlockState displayTile = entity.getDisplayTile();

		if (displayTile.getRenderType() != EnumBlockRenderType.INVISIBLE) {
			GlStateManager.pushMatrix();
			bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.scale(0.75, 0.75, 0.75);
			GlStateManager.translate(-0.5, (double) (entity.getDisplayTileOffset() - 8) / 16, 0.5);
			renderCartContents(entity, partialTicks, displayTile);
			GlStateManager.popMatrix();
			GlStateManager.color(1, 1, 1, 1);
			bindEntityTexture(entity);
		}

		GlStateManager.scale(-1, -1, 1);
		modelMinecart.render(entity, 0, 0, -0.1F, 0, 0, 0.0625F);
		GlStateManager.popMatrix();

		if (renderOutlines) {
			GlStateManager.disableOutlineMode();
			GlStateManager.disableColorMaterial();
		}

		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	public void doRender(final T entity, double x, double y, double z, float entityYaw, final float partialTicks) {
//		temp(entity, x, y, z, entityYaw, partialTicks);
//		if (true) return;

		GlStateManager.pushMatrix();
		bindEntityTexture(entity);
		final double deltaPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
		final double deltaPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
		final double deltaPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

		final Vec3d entityPos = entity.getPos(deltaPosX, deltaPosY, deltaPosZ);

		final float entityPitch;
		if (entityPos != null) {
			Vec3d entityPosOffsetP = entity.getPosOffset(deltaPosX, deltaPosY, deltaPosZ, 0.30000001192092896);
			Vec3d entityPosOffsetN = entity.getPosOffset(deltaPosX, deltaPosY, deltaPosZ, -0.30000001192092896);

			if (entityPosOffsetP == null) {
				entityPosOffsetP = entityPos;
			}

			if (entityPosOffsetN == null) {
				entityPosOffsetN = entityPos;
			}

			x += entityPos.x - deltaPosX;
			y += (entityPosOffsetP.y + entityPosOffsetN.y) / 2 - deltaPosY;
			z += entityPos.z - deltaPosZ;
			Vec3d vec3d3 = entityPosOffsetN.add(-entityPosOffsetP.x, -entityPosOffsetP.y, -entityPosOffsetP.z);

			if (vec3d3.length() != 0) {
				vec3d3 = vec3d3.normalize();
				entityYaw = (float) (Math.atan2(vec3d3.z, vec3d3.x) * 180 / Math.PI);
				entityPitch = (float) (Math.atan(vec3d3.y) * 73);
			} else entityPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;

		} else entityPitch = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;

		GlStateManager.translate(x, y + 0.375, z);
		GlStateManager.rotate(180 - entityYaw, 0, 1, 0);
		GlStateManager.rotate(-entityPitch, 0, 0, 1);
		final float rollAngle = entity.getRollingAmplitude() - partialTicks;
		float damageAmount = entity.getDamage() - partialTicks;

		if (damageAmount < 0) {
			damageAmount = 0;
		}

		if (rollAngle > 0) {
			GlStateManager.rotate(MathHelper.sin(rollAngle) * rollAngle * damageAmount / 10 * entity.getRollingDirection(), 1, 0, 0);
		}

		int displayTileOffset = entity.getDisplayTileOffset();

		if (renderOutlines) {
			GlStateManager.enableColorMaterial();
			GlStateManager.enableOutlineMode(getTeamColor(entity));
		}

		final IBlockState displayTile = entity.getDisplayTile();

		if (displayTile.getRenderType() != EnumBlockRenderType.INVISIBLE) {
			GlStateManager.pushMatrix();
			bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.scale(0.75, 0.75, 0.75);
			GlStateManager.translate(-0.5, (double) (displayTileOffset - 8) / 16, 0.5);
			renderCartContents(entity, partialTicks, displayTile);
			GlStateManager.popMatrix();
			GlStateManager.color(1, 1, 1, 1);
			bindEntityTexture(entity);
		}

		GlStateManager.scale(-1, -1, 1);
		modelMinecart.render(entity, 0, 0, -0.1F, 0, 0, 0.0625F);
		GlStateManager.popMatrix();

		if (renderOutlines) {
			GlStateManager.disableOutlineMode();
			GlStateManager.disableColorMaterial();
		}

		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(final T entity) {
		return MINECART_TEXTURES;
	}

	protected void renderCartContents(final T cart, final float partialTicks, final IBlockState blockState) {
		GlStateManager.pushMatrix();
		Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(blockState, cart.getBrightness());
		GlStateManager.popMatrix();
	}
}
