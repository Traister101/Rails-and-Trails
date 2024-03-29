package mod.traister101.rnt.objects.items;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import mod.traister101.rnt.objects.blocks.BlocksRNT;
import mod.traister101.rnt.objects.blocks.RoadSlab;
import mod.traister101.rnt.objects.blocks.RoadStairs;
import mod.traister101.rnt.objects.types.MinecartMetal;
import net.dries007.tfc.api.registries.TFCRegistries;
import net.dries007.tfc.api.types.Tree;
import net.dries007.tfc.objects.items.itemblock.ItemBlockTFC;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistry;

import static mod.traister101.rnt.RailsNTrails.MODID;
import static net.dries007.tfc.objects.CreativeTabsTFC.CT_DECORATIONS;
import static net.dries007.tfc.util.Helpers.getNull;

@ObjectHolder(MODID)
@EventBusSubscriber(modid = MODID)
public final class ItemsRNT {

	@ObjectHolder("minecart/bronze")
	public static final ItemMinecartRideable BRONZE_MINECART = getNull();
	@ObjectHolder("minecart/wrought_iron")
	public static final ItemMinecartRideable WROUGHT_IRON_MINECART = getNull();
	@ObjectHolder("minecart/steel")
	public static final Item STEEL_MINECART = getNull();
	private static ImmutableList<Item> allSimpleItems;

	public static ImmutableList<Item> getAllSimpleItems() {
		return allSimpleItems;
	}

	@SubscribeEvent
	public static void registerItems(final Register<Item> event) {

		final IForgeRegistry<Item> registry = event.getRegistry();
		final Builder<Item> simpleItems = ImmutableList.builder();

		simpleItems.add(register(registry, "minecart/steel", new ItemMinecartRideable(MinecartMetal.STEEL), CreativeTabs.TRANSPORTATION));

		for (final Tree wood : TFCRegistries.TREES) {
			//noinspection ConstantConditions
			simpleItems.add(register(registry,
					"minecart/steel/chest/" + wood.getRegistryName().getPath(),
					new ItemMinecartChest(wood, MinecartMetal.STEEL), CreativeTabs.TRANSPORTATION));
		}

		BlocksRNT.getAllNormalItemBlocks().forEach(x -> registerItemBlock(registry, x));
		BlocksRNT.getAllInventoryItemBlocks().forEach(x -> registerItemBlock(registry, x));

		for (final RoadSlab.Half slab : BlocksRNT.getAllSlabBlocks()) {
			//noinspection ConstantConditions
			simpleItems.add(register(registry,
					slab.getRegistryName().getPath(),
					new ItemRoadSlab(slab, slab, slab.doubleSlab),
					CT_DECORATIONS));
			OreDictionary.registerOre("slab", slab);
		}

		for (final RoadStairs stairs : BlocksRNT.getAllStairsBlocks()) {
			//noinspection ConstantConditions
			simpleItems.add(register(registry,
					stairs.getRegistryName().getPath(),
					new ItemBlockTFC(stairs), CT_DECORATIONS));
			OreDictionary.registerOre("stair", stairs);
		}

		allSimpleItems = simpleItems.build();
	}

	@SuppressWarnings("ConstantConditions")
	private static void registerItemBlock(final IForgeRegistry<Item> r, final ItemBlock item) {
		item.setRegistryName(item.getBlock().getRegistryName());
		item.setCreativeTab(item.getBlock().getCreativeTab());
		r.register(item);
	}

	private static <T extends Item> T register(final IForgeRegistry<Item> r, final String name, final T item, final CreativeTabs ct) {
		item.setRegistryName(MODID, name);
		item.setTranslationKey(MODID + "." + name.replace('/', '.'));
		item.setCreativeTab(ct);
		r.register(item);
		return item;
	}
}