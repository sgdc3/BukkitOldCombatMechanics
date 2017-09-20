package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ModuleProjectileKnockback extends Module {

    private double snowballDamage;
    private double eggDamage;
    private double enderpearlDamage;

    public ModuleProjectileKnockback(OldCombatMechanics plugin) {
        super(plugin, "projectile-knockback");

        snowballDamage = getConfiguration().getDouble("damage.snowball");
        eggDamage = getConfiguration().getDouble("damage.egg");
        enderpearlDamage = getConfiguration().getDouble("damage.ender_pearl");
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityHit(EntityDamageByEntityEvent event) {
        if (!shouldActivate(event.getEntity())) {
            return;
        }

        EntityType type = event.getDamager().getType();
        switch (type) {
            case SNOWBALL:
                event.setDamage(snowballDamage);
                break;
            case EGG:
                event.setDamage(eggDamage);
                break;
            case ENDER_PEARL:
                event.setDamage(enderpearlDamage);
                break;
            default:
                break;
        }
    }
}