package gvlfm78.plugin.oldcombatmechanics.utils.damages;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class MobDamage {

    private static HashMap<EntityType, Enchantment> enchants = new HashMap<>();

    static {
        enchants.put(EntityType.SKELETON, Enchantment.DAMAGE_UNDEAD);
        enchants.put(EntityType.ZOMBIE, Enchantment.DAMAGE_UNDEAD);
        enchants.put(EntityType.WITHER, Enchantment.DAMAGE_UNDEAD);
        enchants.put(EntityType.PIG_ZOMBIE, Enchantment.DAMAGE_UNDEAD);

        enchants.put(EntityType.SPIDER, Enchantment.DAMAGE_ARTHROPODS);
        enchants.put(EntityType.CAVE_SPIDER, Enchantment.DAMAGE_ARTHROPODS);
        enchants.put(EntityType.SILVERFISH, Enchantment.DAMAGE_ARTHROPODS);
        enchants.put(EntityType.ENDERMITE, Enchantment.DAMAGE_ARTHROPODS);
    }

    public static Enchantment getEnchant(EntityType entity) {
        return (enchants.getOrDefault(entity, null));
    }

    public static double applyEntityBasedDamage(EntityType entity, ItemStack item, double startDamage) {
        Enchantment enchantment = getEnchant(entity);
        if (enchantment == null) {
            return startDamage;
        }

        if (enchantment == Enchantment.DAMAGE_UNDEAD || enchantment == Enchantment.DAMAGE_ARTHROPODS) {
            return startDamage + 2.5 * item.getEnchantmentLevel(enchantment);
        }

        return startDamage;
    }
}