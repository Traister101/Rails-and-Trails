package mod.traister101.rnt.objects.items;

import mod.traister101.rnt.objects.blocks.BlocksRNT;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import static mod.traister101.rnt.RailsNTrails.MODID;

@ObjectHolder(MODID)
@EventBusSubscriber(modid = MODID)
public final class ItemsRNT {

    @SubscribeEvent
    public static void registerItems(final Register<Item> event) {

        final IForgeRegistry<Item> registry = event.getRegistry();

        BlocksRNT.getAllNormalItemBlocks().forEach(x -> registerItemBlock(registry, x));
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