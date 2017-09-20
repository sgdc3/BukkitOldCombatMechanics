package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.RegisteredListener;

import java.util.Collection;
import java.util.EnumMap;

public class ModuleFishingKnockback extends Module {

    private double damage;
    private boolean checkCancelled;
    private boolean useEntityDamageEvent;

    public ModuleFishingKnockback(OldCombatMechanics plugin) {
        super(plugin, "old-fishing-knockback");

        damage = getConfiguration().getDouble("damage");
        if (damage < 0) {
            damage = 0.2;
        }
        checkCancelled = getConfiguration().getBoolean("checkCancelled");
        useEntityDamageEvent = getConfiguration().getBoolean("useEntityDamageEvent");
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRodLand(ProjectileHitEvent event) {
        Entity hookEntity = event.getEntity();

        if (!shouldActivate(hookEntity)) {
            return;
        }

        if (event.getEntityType() != EntityType.FISHING_HOOK) {
            return;
        }
        FishHook hook = (FishHook) hookEntity;

        Entity hitEntity = null;
        try {
            hitEntity = event.getHitEntity();
        } catch (NoSuchMethodError e) { //For older version that don't have such method
            Collection<Entity> entities = hook.getNearbyEntities(0.25, 0.25, 0.25);
            for (Entity entity : entities) {
                if (entity instanceof Player) {
                    hitEntity = entity;
                    break;
                }
            }
        }
        if (hitEntity == null) {
            return;
        }

        if (!(hitEntity instanceof Player)) {
            return;
        }
        Player shooter = (Player) hook.getShooter();
        Player victim = (Player) hitEntity;

        debug("You were hit by a fishing rod!", victim);

        if (victim.getUniqueId().equals(shooter.getUniqueId())) {
            return;
        }

        if (victim.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        EntityDamageEvent damageEvent = makeEvent(shooter, victim, damage);
        Bukkit.getPluginManager().callEvent(damageEvent);

        if (checkCancelled && damageEvent.isCancelled()) {
            //This is to check what plugins are listening to the event
            if (OldCombatMechanics.isDebugEnabled()) {
                debug("You can't do that here!", shooter);
                HandlerList handlers = damageEvent.getHandlers();

                for (RegisteredListener handler : handlers.getRegisteredListeners()) {
                    debug("Plugin Listening: " + handler.getPlugin().getName(), shooter);
                }
            }
            return;
        }

        victim.damage(damage);

        Location location = victim.getLocation().add(0, 0.5, 0);
        victim.teleport(location);
        victim.setVelocity(location.subtract(shooter.getLocation()).toVector().normalize().multiply(0.4));
    }

    @SuppressWarnings("unchecked")
    private EntityDamageEvent makeEvent(Player rodder, Player player, double damage) {
        if (useEntityDamageEvent) {
            return new EntityDamageEvent(
                    player,
                    EntityDamageEvent.DamageCause.PROJECTILE,
                    new EnumMap(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, damage)),
                    new EnumMap(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, Functions.constant(damage)))
            );
        } else {
            return new EntityDamageByEntityEvent(
                    rodder,
                    player,
                    EntityDamageEvent.DamageCause.PROJECTILE,
                    new EnumMap(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, damage)),
                    new EnumMap(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, Functions.constant(damage)))
            );
        }
    }
}