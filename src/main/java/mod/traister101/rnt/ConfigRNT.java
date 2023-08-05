package mod.traister101.rnt;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.*;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static mod.traister101.rnt.RailsNTrails.MODID;
import static mod.traister101.rnt.RailsNTrails.NAME;

@EventBusSubscriber
@Config(modid = MODID, type = Type.INSTANCE, name = NAME)
public final class ConfigRNT {

	@Comment("Road Configs")
	@LangKey("config.rnt.road")
	public static final RoadConfig ROAD_CONFIG = new RoadConfig();

	@SubscribeEvent
	public static void onConfigChangedEvent(final OnConfigChangedEvent event) {
		if (event.getModID().equals(MODID)) {
			ConfigManager.sync(MODID, Type.INSTANCE);
		}
	}

	public static final class RoadConfig {

		@Comment("Controls the move speed modifier when on road blocks. This value is directly used so 1.2 is a 20% bonus, like speed 1")
		@LangKey("config.rnt.road.move_speed_modifier")
		@SlidingOption()
		@RangeDouble(min = 0, max = 10)
		public double moveSpeedModifier = 1.2;
	}
}
