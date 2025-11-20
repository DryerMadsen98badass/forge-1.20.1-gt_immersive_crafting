package net.mads.immersive_crafting;

import java.util.List;

public class InputItem {
    public final String item;        // f.eks "gtceu:iron_screw"
    public final int count;          // hvor mange som trengs
    public final int strengthLevel;  // 1-15
    public final List<String> tags;  // f.eks ["forge:strength/2", "forge:iron"]

    public InputItem(String item, int count, int strengthLevel, List<String> tags) {
        this.item = item;
        this.count = count;
        this.strengthLevel = strengthLevel;
        this.tags = tags;
    }
}
