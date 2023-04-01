package mod.traister101.rnt.client;

import mod.traister101.rnt.objects.blocks.BlocksRNT;
import mod.traister101.rnt.objects.blocks.RoadSlab;
import mod.traister101.rnt.objects.entities.EntitySteelMinecart;
import mod.traister101.rnt.objects.items.ItemsRNT;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static mod.traister101.rnt.RailsNTrails.MODID;

@SideOnly(Side.CLIENT)
@EventBusSubscriber(value = Side.CLIENT, modid = MODID)
public final class ClientRegistry {

	public static void preInit() {
		RenderingRegistry.registerEntityRenderingHandler(EntitySteelMinecart.class, RenderMinecart<EntitySteelMinecart>::new);
	}

	@SubscribeEvent
	public static void registerModels(final ModelRegistryEvent event) {

		// Item Blocks
		for (final ItemBlock item : BlocksRNT.getAllNormalItemBlocks())
			registerItemRenderer(item, 0, "normal");

		// Basic items
		for (final Item item : ItemsRNT.getAllSimpleItems())
			registerBasicItemRenderer(item);

		// Wonky slab state registration
		for (RoadSlab.Half slab : BlocksRNT.getAllSlabBlocks()) {
			ModelLoader.setCustomStateMapper(slab, new StateMap.Builder().ignore(RoadSlab.VARIANT).build());
			ModelLoader.setCustomStateMapper(slab.doubleSlab, new StateMap.Builder().ignore(RoadSlab.VARIANT).build());
		}
	}

	@SuppressWarnings("ConstantConditions")
	private static void registerBasicItemRenderer(Item item) {
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName().toString()));
	}

	@SuppressWarnings({"SameParameterValue", "ConstantConditions"})
	private static void registerItemRenderer(Item item, int meta, String id) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id));
	}
}