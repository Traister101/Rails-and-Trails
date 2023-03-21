package mod.traister101.rnt.client;

import mod.traister101.rnt.objects.blocks.BlocksRNT;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static mod.traister101.rnt.RailsNTrails.MODID;

@SideOnly(Side.CLIENT)
@EventBusSubscriber(value = Side.CLIENT, modid = MODID)
public final class ClientRegistry {

    @SubscribeEvent
    public static void registerModels(final ModelRegistryEvent event) {

        // Item Blocks
        for (final ItemBlock item : BlocksRNT.getAllNormalItemBlocks())
            ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "normal"));
    }
}