package mod.traister101.rnt;

import mod.traister101.rnt.client.ClientRegistry;
import mod.traister101.rnt.objects.entities.EntitiesRNT;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static mod.traister101.rnt.RailsNTrails.*;

@SuppressWarnings("FieldMayBeFinal")
@Mod(modid = MODID, name = NAME, version = VERSION, useMetadata = true)
public final class RailsNTrails {
    public static final String MODID = "@MODID@";
    public static final String NAME = "@MODNAME@";
    public static final String VERSION = "@VERSION@";

    @Instance
    private static RailsNTrails INSTANCE = null;
    private final Logger log = LogManager.getLogger(MODID);
    private SimpleNetworkWrapper network;

    public static RailsNTrails getInstance() {
        return INSTANCE;
    }

    public Logger getLog() {
        return INSTANCE.log;
    }

    public SimpleNetworkWrapper getNetwork() {
        return INSTANCE.network;
    }

    @EventHandler
    public void preInit(final FMLPreInitializationEvent event) {

        EntitiesRNT.preInit();

        if (event.getSide().isClient()) {
            ClientRegistry.preInit();
        }
    }
}