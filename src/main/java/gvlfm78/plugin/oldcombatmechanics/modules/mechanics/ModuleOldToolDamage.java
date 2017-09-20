package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import gvlfm78.plugin.oldcombatmechanics.utils.ItemUtils;
import gvlfm78.plugin.oldcombatmechanics.utils.Messenger;
import gvlfm78.plugin.oldcombatmechanics.utils.damages.MobDamage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ModuleOldToolDamage extends Module {

    private final static String[] WEAPONS = {"sword", "axe", "pickaxe", "spade", "hoe"};

    private HashMap<String, Double> damages;

    public ModuleOldToolDamage(OldCombatMechanics plugin) {
        super(plugin, "old-tool-damage");

        damages = new HashMap<>();
        ConfigurationSection damageSection = getConfiguration().getConfigurationSection("damages");
        for (String key : damageSection.getKeys(false)) {
            double val = damageSection.getDouble(key);
            Messenger.debug("Loading damage '" + val + "' for type '" + key + "'");
            damages.put(key, damageSection.getDouble(key));
        }
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    private double getDamage(Material material) {
        if (!damages.containsKey(material.name())) {
            return -1;
        }
        return damages.get(material.name());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamaged(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof Player)) {
            return;
        }
        Player player = (Player) damager;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) {
            return;
        }
        Material material = item.getType();

        if (!isWeapon(material) || !shouldActivate(damager)) {
            return;
        }

        EntityType entity = event.getEntityType();

        double baseDamage = event.getDamage();
        double enchantmentDamage = (MobDamage.applyEntityBasedDamage(entity, item, baseDamage)
                + getSharpnessDamage(item.getEnchantmentLevel(Enchantment.DAMAGE_ALL))) - baseDamage;

        double divider = getDamage(material);
        if (divider <= 0) {
            divider = 1;
        }
        double newDamage = (baseDamage - enchantmentDamage) / divider;
        newDamage += enchantmentDamage;//Re-add damage from enchantments
        if (newDamage < 0) {
            newDamage = 0;
        }
        event.setDamage(newDamage);

        debug("Item: " + material.toString() + " Old Damage: " + baseDamage
                        + " Enchantment Damage: " + enchantmentDamage
                        + " Divider: " + divider + " Afterwards damage: " + event.getFinalDamage()
                        + " ======== New damage: " + newDamage
                , player);
    }

    private double getSharpnessDamage(int level) {
        return level >= 1 ? level * 1.25 : 0;
    }


    private boolean isWeapon(Material material) {
        if (material == null) {
            return false;
        }

        boolean hasAny = false;
        for (String type : WEAPONS) {
            if (ItemUtils.materialEndsWith(material, type)) {
                hasAny = true;
            }
        }
        return hasAny;
    }
}