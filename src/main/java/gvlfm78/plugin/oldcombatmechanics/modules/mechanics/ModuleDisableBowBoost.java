package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class ModuleDisableBowBoost extends Module {

    public ModuleDisableBowBoost(OldCombatMechanics plugin) {
        super(plugin, "disable-bow-boost");
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();

        if (entity == null || !(entity instanceof Player) || damager.getType() != EntityType.ARROW) {
            return;
        }

        Player player = (Player) entity;
        Arrow arrow = (Arrow) damager;

        if (!shouldActivate(entity)) {
            return;
        }

        ProjectileSource projectileSource = arrow.getShooter();
        if (!(projectileSource instanceof Player)) {
            return;
        }

        Player shooter = (Player) projectileSource;
        if (player.getUniqueId().equals(shooter.getUniqueId())) {
            event.setCancelled(true);
            debug("We cancelled your bow boost", player);
        }
    }
}