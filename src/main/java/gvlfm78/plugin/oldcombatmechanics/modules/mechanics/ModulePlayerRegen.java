package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import gvlfm78.plugin.oldcombatmechanics.utils.MathUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModulePlayerRegen extends Module {

    private Map<UUID, Long> healTimes;

    private long frequency;
    private float exhaustion;

    public ModulePlayerRegen(OldCombatMechanics plugin) {
        super(plugin, "old-player-regen");

        frequency = getConfiguration().getLong("frequency");
        exhaustion = (float) getConfiguration().getDouble("exhaustion");
    }

    @Override
    public void onEnable() {
        healTimes = new HashMap<>();
    }

    @Override
    public void onDisable() {
        healTimes = null;
    }

    private long getLastHealTime(Player p) {
        if (!healTimes.containsKey(p.getUniqueId())) {
            healTimes.put(p.getUniqueId(), System.currentTimeMillis() / 1000);
        }

        return healTimes.get(p.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRegen(EntityRegainHealthEvent event) {
        if (event.getEntityType() != EntityType.PLAYER || event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) {
            return;
        }
        final Player player = (Player) event.getEntity();

        if (!shouldActivate(player)) {
            return;
        }

        event.setCancelled(true);

        long currentTime = System.currentTimeMillis() / 1000;
        long lastHealTime = getLastHealTime(player);

        if (currentTime - lastHealTime < frequency) {
            return;
        }

        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

        if (player.getHealth() < maxHealth) {
            player.setHealth(MathUtils.clamp(player.getHealth() + getConfiguration().getInt("amount"), 0.0, maxHealth));
            healTimes.put(player.getUniqueId(), currentTime);
        }

        final float previousExhaustion = player.getExhaustion();
        final float exhaustionToApply = exhaustion;

        schedule(new BukkitRunnable() {

            @Override
            public void run() {
                //This is because bukkit doesn't stop the exhaustion change when cancelling the event
                player.setExhaustion(previousExhaustion + exhaustionToApply);
                debug("Exhaustion before: " + previousExhaustion + " Now: " + player.getExhaustion() + "Saturation: " + player.getSaturation(), player);
            }
        });
    }
}