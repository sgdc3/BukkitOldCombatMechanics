package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;

public class ModuleDisableProjectileRandomness extends Module {

    public ModuleDisableProjectileRandomness(OldCombatMechanics plugin) {
        super(plugin, "disable-projectile-randomness");
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        ProjectileSource shooter = projectile.getShooter();

        if (!(shooter instanceof Player)) {
            return;
        }
        Player player = (Player) shooter;

        if (!shouldActivate(projectile)) {
            return;
        }

        debug("Making projectile go straight", player);
        //Here we get a unit vector of the direction the player is looking in and multiply it by the projectile's vector's magnitude
        //We then assign this to the projectile as its new velocity
        projectile.setVelocity(player.getLocation().getDirection().normalize().multiply(projectile.getVelocity().length()));
    }
}