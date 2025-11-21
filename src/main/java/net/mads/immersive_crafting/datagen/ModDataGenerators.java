package net.mads.immersive_crafting.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

/**
 * Registrerer datagenerator‑providers under GatherDataEvent.
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModDataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        PackOutput output = event.getGenerator().getOutput();
        ExistingFileHelper helper = event.getExistingFileHelper();

        // NB: 2. parameter for GticItemTagProvider-konstruktøren i min klasse var
        // CompletableFuture<net.minecraft.data.PackOutput> lookupProvider som en placeholder.
        // Pass inn en completed future for nå (tilpass hvis du endrer signatur).
        CompletableFuture<PackOutput> cf = CompletableFuture.completedFuture(output);

        event.getGenerator().addProvider(true, new GticItemTagProvider(output, cf, "gtic", helper));
    }
}