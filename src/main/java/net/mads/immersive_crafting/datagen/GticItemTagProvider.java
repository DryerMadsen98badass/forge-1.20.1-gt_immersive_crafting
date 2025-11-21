package net.mads.immersive_crafting.datagen;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Datagenerator som genererer gtic-tags for komponenter og cable/wire-variants.
 * Leser StrengthVerdi- og Strength JSON fra /data/gtic/strengths/*.json på classpath.
 *
 * Legg denne til i din data-gen registrator (GatherDataEvent).
 */
public class GticItemTagProvider extends ItemTagsProvider {

    private static final List<String> COMPONENTS = ImmutableList.of(
            "plates", "rods", "bolts", "rings",
            "tiny_pipes", "normal_pipes", "large_pipes", "huge_pipes",
            "gears", "foils", "small_gears", "long_rods", "rotors"
    );

    private static final Map<String, String> COMPONENT_TO_SUFFIX = Map.ofEntries(
            Map.entry("plates", "plate"),
            Map.entry("rods", "rod"),
            Map.entry("bolts", "bolt"),
            Map.entry("rings", "ring"),
            Map.entry("tiny_pipes", "tiny_pipe"),
            Map.entry("normal_pipes", "normal_pipe"),
            Map.entry("large_pipes", "large_pipe"),
            Map.entry("huge_pipes", "huge_pipe"),
            Map.entry("gears", "gear"),
            Map.entry("foils", "foil"),
            Map.entry("small_gears", "small_gear"),
            Map.entry("long_rods", "long_rod"),
            Map.entry("rotors", "rotor")
    );

    private static final List<String> CABLE_SIZES = ImmutableList.of("single","double","quadruple","hex","octuple");
    private static final List<String> WIRE_SIZES = ImmutableList.of("single","double","quadruple","hex","octuple");

    // Rekkefølge fra høyest tier til lavest som vi emiterer nedover
    private static final List<String> TIER_ORDER = List.of("luv","iv","ev","hv","mv","lv");

    private final Gson gson = new Gson();

    // NB: Behold signaturen som passer ditt Forge/Minecraft-miljø.
    // Du kan bruke den signaturen du allerede har fungerende i prosjektet.
    public GticItemTagProvider(PackOutput output, CompletableFuture<net.minecraft.data.PackOutput> lookupProvider, String modid, ExistingFileHelper helper) {
        super(output, ForgeRegistries.ITEMS, lookupProvider, modid, helper);
    }

    @Override
    protected void addTags() {
        // Les strengths fra JSON (hvis fil mangler, fallback til tom liste)
        List<StrengthVerdiEntry> strengthEntries = loadComponentStrengthEntries();
        List<WireStrengthEntry> wireEntries = loadWireStrengthEntries();

        // Generer komponent-tags
        for (StrengthVerdiEntry entry : strengthEntries) {
            generateComponentTagsFor(entry);
        }

        // Generer cable/wire-tags
        for (WireStrengthEntry we : wireEntries) {
            generateCableWireTagsFor(we);
        }
    }

    private List<StrengthVerdiEntry> loadComponentStrengthEntries() {
        try (InputStream is = getClass().getResourceAsStream("/data/gtic/strengths/components.json")) {
            if (is == null) return Collections.emptyList();
            Type type = new TypeToken<List<StrengthVerdiEntry>>(){}.getType();
            return gson.fromJson(new InputStreamReader(is), type);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<WireStrengthEntry> loadWireStrengthEntries() {
        try (InputStream is = getClass().getResourceAsStream("/data/gtic/strengths/wires.json")) {
            if (is == null) return Collections.emptyList();
            Type type = new TypeToken<List<WireStrengthEntry>>(){}.getType();
            return gson.fromJson(new InputStreamReader(is), type);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private void generateComponentTagsFor(StrengthVerdiEntry entry) {
        int strength = entry.strength;
        String sourceMod = entry.mod;

        for (String material : entry.materials) {
            for (String compPlural : COMPONENTS) {
                String suffix = COMPONENT_TO_SUFFIX.get(compPlural);
                if (suffix == null) continue;

                for (int k = strength; k >= 1; k--) {
                    TagKey<Item> tagKey = TagKey.create(net.minecraft.tags.TagUtil.ITEM_TAGS_REGISTRY, new ResourceLocation("gtic", compPlural + "/" + k));
                    List<ResourceLocation> candidates = candidateItemNamesForComponent(sourceMod, material, suffix);
                    for (ResourceLocation cand : candidates) {
                        Item item = ForgeRegistries.ITEMS.getValue(cand);
                        if (item != null) {
                            this.tag(tagKey).add(item);
                        }
                    }
                }
            }
        }
    }

    private void generateCableWireTagsFor(WireStrengthEntry entry) {
        String tier = entry.tier; // eks: "hv" eller "ev"
        List<String> tiersToEmit = tiersDownFrom(tier);

        for (String material : entry.materials) {
            for (String size : CABLE_SIZES) {
                for (String t : tiersToEmit) {
                    TagKey<Item> tagKey = TagKey.create(net.minecraft.tags.TagUtil.ITEM_TAGS_REGISTRY, new ResourceLocation("gtic", "cable_" + size + "/" + t));
                    ResourceLocation cand = new ResourceLocation("gtceu", material + "_" + size + "_cable");
                    Item item = ForgeRegistries.ITEMS.getValue(cand);
                    if (item != null) this.tag(tagKey).add(item);
                }
            }
            for (String size : WIRE_SIZES) {
                for (String t : tiersToEmit) {
                    TagKey<Item> tagKey = TagKey.create(net.minecraft.tags.TagUtil.ITEM_TAGS_REGISTRY, new ResourceLocation("gtic", "wire_" + size + "/" + t));
                    ResourceLocation cand = new ResourceLocation("gtceu", material + "_" + size + "_wire");
                    Item item = ForgeRegistries.ITEMS.getValue(cand);
                    if (item != null) this.tag(tagKey).add(item);
                }
            }
        }
    }

    private List<String> tiersDownFrom(String tier) {
        int idx = TIER_ORDER.indexOf(tier);
        if (idx == -1) {
            // hvis caller brukte numerisk streng, fallback til tier selv
            return List.of(tier);
        }
        return TIER_ORDER.subList(idx, TIER_ORDER.size());
    }

    private List<ResourceLocation> candidateItemNamesForComponent(String mod, String material, String suffix) {
        List<String> patterns = List.of(
                material + "_" + suffix,           // steel_rod
                material + suffix,                 // steelrod
                suffix + "_" + material            // rod_steel
        );
        return patterns.stream().map(p -> new ResourceLocation(mod, p)).collect(Collectors.toList());
    }

    private static class StrengthVerdiEntry {
        String mod;
        int strength;
        List<String> materials;
    }
    private static class WireStrengthEntry {
        String tier;
        List<String> materials;
    }
}