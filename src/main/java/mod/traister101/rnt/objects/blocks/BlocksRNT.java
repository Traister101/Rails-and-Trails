package mod.traister101.rnt.objects.blocks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import net.dries007.tfc.api.registries.TFCRegistries;
import net.dries007.tfc.api.types.Rock;
import net.dries007.tfc.objects.items.itemblock.ItemBlockTFC;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collection;

import static mod.traister101.rnt.RailsNTrails.MODID;
import static net.dries007.tfc.objects.CreativeTabsTFC.CT_DECORATIONS;
import static net.dries007.tfc.objects.CreativeTabsTFC.CT_ROCK_BLOCKS;

@ObjectHolder(MODID)
@EventBusSubscriber(modid = MODID)
public final class BlocksRNT {

	private static ImmutableList<RoadSlab.Half> allSlabBlocks;

	private static ImmutableList<ItemBlock> allNormalItemBlocks;
	private static ImmutableList<RoadStairs> allStairsBlocks;

	public static ImmutableList<RoadSlab.Half> getAllSlabBlocks() {
		return allSlabBlocks;
	}

	public static ImmutableList<ItemBlock> getAllNormalItemBlocks() {
		return allNormalItemBlocks;
	}

	@SubscribeEvent
	@SuppressWarnings("ConstantConditions")
	public static void registerBlocks(Register<Block> event) {

		final IForgeRegistry<Block> registry = event.getRegistry();

		Builder<ItemBlock> normalItemBlocks = ImmutableList.builder();

		final Collection<Rock> registeredRocks = TFCRegistries.ROCKS.getValuesCollection();

		// Register a road for each block type, allows us to easily support things such as Rocks+
		for (Rock rock : registeredRocks) {
			normalItemBlocks.add(new ItemBlockTFC(register(registry,
			                                               "road/" + rock.getRegistryName().getPath(),
			                                               new Road(rock), CT_ROCK_BLOCKS)));
		}

		{
			Builder<RoadStairs> stairs = new Builder<>();
			Builder<RoadSlab.Half> slabs = new Builder<>();

			// Stairs
			for (Rock rock : registeredRocks) {
				stairs.add(register(registry,
				                    "stairs/road/" + rock.getRegistryName().getPath(),
				                    new RoadStairs(rock), CT_DECORATIONS));
			}

			// Full block slabs, managed by the half's
			for (Rock rock : registeredRocks) {
				register(registry,
				         "double_slab/road/" + rock.getRegistryName().getPath(),
				         new RoadSlab.Double(rock), CT_DECORATIONS);
			}

			// Half slabs
			for (Rock rock : registeredRocks) {
				slabs.add(register(registry,
				                   "slab/road/" + rock.getRegistryName().getPath(),
				                   new RoadSlab.Half(rock), CT_DECORATIONS));
			}

			allSlabBlocks = slabs.build();
			allStairsBlocks = stairs.build();
		}

		allNormalItemBlocks = normalItemBlocks.build();
	}

	private static <T extends Block> T register(IForgeRegistry<Block> registry, String name, T block, CreativeTabs creativeTab) {
		block.setCreativeTab(creativeTab);
		return register(registry, name, block);
	}

	private static <T extends Block> T register(IForgeRegistry<Block> r, String name, T block) {
		block.setRegistryName(MODID, name);
		block.setTranslationKey(MODID + "." + name.replace('/', '.'));
		r.register(block);
		return block;
	}

	public static ImmutableList<RoadStairs> getAllStairsBlocks() {
		return allStairsBlocks;
	}
}