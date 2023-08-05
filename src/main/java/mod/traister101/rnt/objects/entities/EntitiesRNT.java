package mod.traister101.rnt.objects.entities;

import mcp.MethodsReturnNonnullByDefault;
import mod.traister101.rnt.RailsNTrails;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import javax.annotation.ParametersAreNonnullByDefault;

import static mod.traister101.rnt.RailsNTrails.MODID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@EventBusSubscriber(modid = MODID)
public final class EntitiesRNT {

	private static int id = 1; // don't use id 0, it's easier to debug if something goes wrong

	public static void preInit() {
		registerMinecart("steel_minecart", EntityMinecartRideableRNT.class);
		registerMinecart("steel_minecart_chest", EntityMinecartChestRNT.class);
	}

	@SuppressWarnings("SameParameterValue")
	private static void registerMinecart(String name, Class<? extends Entity> cls) {
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, name), cls, name, id++, RailsNTrails.getInstance(),
				160, 1, true);
	}
}