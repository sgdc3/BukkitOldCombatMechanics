package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import gvlfm78.plugin.oldcombatmechanics.utils.ItemUtils;
import gvlfm78.plugin.oldcombatmechanics.utils.damages.ToolDamage;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ModuleSwordSweep extends Module {

    private SweepTask task;

    public ModuleSwordSweep(OldCombatMechanics plugin) {
        super(plugin, "disable-sword-sweep");
    }

    @Override
    public void onEnable() {
        task = (SweepTask) scheduleTimer(new SweepTask(), 1);
    }

    @Override
    public void onDisable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getDamager();

        if (!shouldActivate(player)) {
            return;
        }

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!ItemUtils.materialEndsWith(weapon.getType(), "sword")) {
            return;
        }

        //Disable sword sweep
        int locHashCode = player.getLocation().hashCode(); // ATTACKER

        int level = 0;
        try { //In a try catch for servers that haven't updated
            level = weapon.getEnchantmentLevel(Enchantment.SWEEPING_EDGE);
        } catch (NoSuchFieldError ignored) {
        }

        float damage = ToolDamage.getDamage(weapon.getType()) * level / (level + 1) + 1;
        if (event.getDamage() == damage) {
            // Possibly a sword-sweep attack
            if (task.contains(locHashCode)) {
                debug("Cancelling sweep...", player);
                event.setCancelled(true);
            }
        } else {
            task.add(locHashCode);
        }

        //ModuleOldToolDamage.onAttack(e); //TODO: fix that
    }

    private class SweepTask extends BukkitRunnable {

        private List<Integer> swordLocations;

        private SweepTask() {
            swordLocations = new ArrayList<>();
        }

        public boolean contains(Integer locHashCode) {
            return swordLocations.contains(locHashCode);
        }

        public boolean add(Integer locHashCode) {
            return swordLocations.add(locHashCode);
        }

        public void run() {
            //Clear buffer
            swordLocations.clear();
        }
    }
}