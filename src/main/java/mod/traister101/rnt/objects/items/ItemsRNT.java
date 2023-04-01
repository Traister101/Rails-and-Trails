package mod.traister101.rnt.objects.items;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import mod.traister101.rnt.objects.blocks.BlocksRNT;
import mod.traister101.rnt.objects.blocks.RoadSlab;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import static mod.traister101.rnt.Helper.getNull;
import static mod.traister101.rnt.RailsNTrails.MODID;
import static net.dries007.tfc.objects.CreativeTabsTFC.CT_DECORATIONS;

@ObjectHolder(MODID)
@EventBusSubscriber(modid = MODID)
public final class ItemsRNT {

	public static final Item STEEL_MINECART = getNull();

	private static ImmutableList<Item> allSimpleItems;

	public static ImmutableList<Item> getAllSimpleItems() {
		return allSimpleItems;
	}

	@SubscribeEvent
	public static void registerItems(final Register<Item> event) {

		final IForgeRegistry<Item> registry = event.getRegistry();
		Builder<Item> simpleItems = ImmutableList.builder();

		simpleItems.add(register(registry, "minecart/steel", new ItemSteelMinecart(), CreativeTabs.TRANSPORTATION));

		BlocksRNT.getAllNormalItemBlocks().forEach(x -> registerItemBlock(registry, x));

		for (RoadSlab.Half slab : BlocksRNT.getAllSlabBlocks()) {
			//noinspection ConstantConditions
			simpleItems.add(register(registry,
			                         slab.getRegistryName().getPath(),
			                         new ItemRoadSlab(slab, slab, slab.doubleSlab),
			                         CT_DECORATIONS));
		}

		allSimpleItems = simpleItems.build();
	}

	@SuppressWarnings("ConstantConditions")
	private static void registerItemBlock(IForgeRegistry<Item> r, ItemBlock item) {
		item.setRegistryName(item.getBlock().getRegistryName());
		item.setCreativeTab(item.getBlock().getCreativeTab());
		r.register(item);
	}

	private static <T extends Item> T register(IForgeRegistry<Item> r, String name, T item, CreativeTabs ct) {
		item.setRegistryName(MODID, name);
		item.setTranslationKey(MODID + "." + name.replace('/', '.'));
		item.setCreativeTab(ct);
		r.register(item);
		return item;
	}
}