package gvlfm78.plugin.oldcombatmechanics.utils.damages;

import org.bukkit.Material;

//Default minecraft tool damages
public enum ToolDamage {

    WOOD_SWORD(4), WOOD_SPADE(2.5F), WOOD_PICKAXE(2), WOOD_AXE(7), WOOD_HOE(1),
    STONE_SWORD(5), STONE_SPADE(3.5F), STONE_PICKAXE(3), STONE_AXE(9), STONE_HOE(1),
    IRON_SWORD(6), IRON_SPADE(4.5F), IRON_PICKAXE(4), IRON_AXE(9), IRON_HOE(1),
    GOLD_SWORD(4), GOLD_SPADE(2.5F), GOLD_PICKAXE(2), GOLD_AXE(7), GOLD_HOE(1),
    DIAMOND_SWORD(7), DIAMOND_SPADE(5.5F), DIAMOND_PICKAXE(5), DIAMOND_AXE(9), DIAMOND_HOE(1);

    private float damage;

    ToolDamage(float damage) {
        this.damage = damage;
    }

    public static float getDamage(String material) {
        return valueOf(material).getDamage();
    }

    public static float getDamage(Material material) {
        return getDamage(material.toString());
    }

    public float getDamage() {
        return damage;
    }
}