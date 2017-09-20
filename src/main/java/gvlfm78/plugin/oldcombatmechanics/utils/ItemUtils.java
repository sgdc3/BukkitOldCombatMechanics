package gvlfm78.plugin.oldcombatmechanics.utils;

import org.bukkit.Material;

public class ItemUtils {

    public static boolean materialEndsWith(Material material, String type) {
        return material.toString().endsWith("_" + type.toUpperCase());
    }
}