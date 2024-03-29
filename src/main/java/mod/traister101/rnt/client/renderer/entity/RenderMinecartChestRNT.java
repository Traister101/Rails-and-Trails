package mod.traister101.rnt.client.renderer.entity;

import mod.traister101.rnt.objects.entities.EntityMinecartChestRNT;
import net.dries007.tfc.api.registries.TFCRegistries;
import net.dries007.tfc.api.types.Tree;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

import static net.dries007.tfc.TerraFirmaCraft.MOD_ID;

@SideOnly(Side.CLIENT)
public class RenderMinecartChestRNT extends RenderMinecartRNT<EntityMinecartChestRNT> {

	/**
	 * Map of chest textures because they have a custom renderer which doesn't work by default
	 */
	private static final Map<Tree, ResourceLocation> CHEST_TEXTURES = new HashMap<>();

	static {
		for (final Tree wood : TFCRegistries.TREES.getValuesCollection()) {
			//noinspection ConstantConditions
			CHEST_TEXTURES.put(wood,
					new ResourceLocation(MOD_ID, "textures/entity/chests/chest/" + wood.getRegistryName().getPath() + ".png"));
		}
	}

	private final ModelChest simpleChest = new ModelChest();

	@SuppressWarnings("unused")
	public RenderMinecartChestRNT(final RenderManager renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public void doRender(final EntityMinecartChestRNT entity, final double x, final double y, final double z, final float entityYaw,
			final float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected void renderCartContents(final EntityMinecartChestRNT cart, final float partialTicks, final IBlockState blockState) {
		GlStateManager.enableDepth();
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);

		bindTexture(CHEST_TEXTURES.get((cart.getWood())));

		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();

		GlStateManager.scale(1, -1, -1);
		GlStateManager.translate(1, -1.1, 0);
		GlStateManager.rotate(270, 0, 1, 0);
		
		final float brightness = cart.getBrightness();
		GlStateManager.color(brightness, brightness, brightness);
		simpleChest.renderAll();

		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}
}