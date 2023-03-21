package mod.traister101.rnt.objects.blocks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import net.dries007.tfc.api.registries.TFCRegistries;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import static mod.traister101.rnt.RailsNTrails.MODID;
import static net.dries007.tfc.objects.CreativeTabsTFC.CT_ROCK_BLOCKS;

@ObjectHolder(MODID)
@EventBusSubscriber(modid = MODID)
public final class BlocksRNT {

    private static ImmutableList<ItemBlock> allNormalItemBlocks;

    public static ImmutableList<ItemBlock> getAllNormalItemBlocks() {
        return allNormalItemBlocks;
    }

    @SubscribeEvent
    public static void registerBlocks(Register<Block> event) {

        final IForgeRegistry<Block> registry = event.getRegistry();

        Builder<ItemBlock> normalItemBlocks = ImmutableList.builder();

        // Register a road for each block type, allows us to easily support things such as Rocks+
        TFCRegistries.ROCKS.forEach(rock -> {
            normalItemBlocks.add(new ItemBlock(register(registry,
                    "road/" + rock.getRegistryName().getPath(),
                    new Road(), CT_ROCK_BLOCKS)));
        });

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
}