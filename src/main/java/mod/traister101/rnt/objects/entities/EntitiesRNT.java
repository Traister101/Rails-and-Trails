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

	public static final String MINECART_RIDEABLE = "minecart_rideable_rnt";
	public static final String MINECART_CHEST = "minecart_chest_rnt";
	public static final String MINECART_BARREL = "minecart_barrel_rnt";
	private static int id = 1; // don't use id 0, it's easier to debug if something goes wrong

	public static void preInit() {
		registerMinecart(MINECART_RIDEABLE, EntityMinecartRideableRNT.class);
		registerMinecart(MINECART_CHEST, EntityMinecartChestRNT.class);
		registerMinecart(MINECART_BARREL, EntityMinecartBarrelRNT.class);
	}

	@SuppressWarnings("SameParameterValue")
	private static void registerMinecart(String name, Class<? extends Entity> cls) {
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, name), cls, name, id++, RailsNTrails.getInstance(),
				160, 1, true);
	}
}