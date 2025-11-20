package net.mads.immersive_crafting;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(immersive_crafting.MOD_ID)
public class immersive_crafting {

    public static final String MOD_ID = "immersive_crafting";
    private static final Logger LOGGER = LogUtils.getLogger();

    public immersive_crafting(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        // Registrerer alt via All-klassene
        AllItems.register(modEventBus);
        AllBlocks.register(modEventBus);
        AllRecipes.register();
    //    AllRecipeTypes.register();

        // Event-listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("ImmersivCrafting mod: Common setup ferdig");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            AllItems.addToCreativeTab(event); // La AllItems håndtere hvilke items som går i creative
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("ImmersivCrafting: Server starting...");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("ImmersivCrafting: Client setup ferdig");
        }
    }
}
