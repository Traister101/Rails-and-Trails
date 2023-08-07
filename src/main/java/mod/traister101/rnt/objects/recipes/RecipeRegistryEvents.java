package mod.traister101.rnt.objects.recipes;

import mod.traister101.rnt.RailsNTrails;
import net.dries007.tfc.objects.recipes.RecipeUtils;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistryModifiable;

@EventBusSubscriber(modid = RailsNTrails.MODID)
public class RecipeRegistryEvents {

	@SubscribeEvent
	public static void onRecipeRegister(final Register<IRecipe> event) {
		final IForgeRegistryModifiable<IRecipe> registry = (IForgeRegistryModifiable<IRecipe>) event.getRegistry();

		RecipeUtils.removeRecipeByName(registry, "tfc", "vanilla/rail/minecart");
		RecipeUtils.removeRecipeByName(registry, "tfc", "vanilla/rail/steel_minecart");
	}
}