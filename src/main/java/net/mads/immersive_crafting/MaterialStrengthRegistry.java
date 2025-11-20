package net.mads.immersive_crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialStrengthRegistry {

    // Map: materialName -> maxStrength
    private static final Map<String, Integer> materialStrengths = new HashMap<>();

    // Registrer materialer med styrke
    static {
        registerMaterial("stainless_steel", 5);
        registerMaterial("iron", 2);
        registerMaterial("gold", 1);
        // legg til flere her
    }

    public static void registerMaterial(String material, int strength) {
        materialStrengths.put(material, strength);
    }

    // Hent alle strength-tags for et materiale
    public static List<String> getStrengthTags(String material) {
        List<String> tags = new ArrayList<>();
        Integer maxStrength = materialStrengths.get(material);
        if (maxStrength == null) return tags;

        for (int i = 1; i <= maxStrength; i++) {
            tags.add("forge:strength/" + i);
        }
        return tags;
    }

    public static boolean hasMaterial(String material) {
        return materialStrengths.containsKey(material);
    }

    public static int getMaxStrength(String material) {
        return materialStrengths.getOrDefault(material, 1);
    }
}
