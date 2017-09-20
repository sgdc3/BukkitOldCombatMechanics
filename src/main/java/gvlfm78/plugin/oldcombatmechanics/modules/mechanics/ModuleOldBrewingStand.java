package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

public class ModuleOldBrewingStand extends Module {

    public ModuleOldBrewingStand(OldCombatMechanics plugin) {
        super(plugin, "old-brewing-stand");
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBrew(BrewEvent event) {
        Block block = event.getBlock();

        if (shouldActivate(block.getWorld()) && block.getType().equals(Material.BREWING_STAND)) { //Just in case...
            ((BrewingStand) block.getState()).setFuelLevel(20);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!shouldActivate(event.getPlayer())) {
            return;
        }

        Inventory inventory = event.getInventory();

        if (inventory == null) {
            return;
        }

        Location location = null;
        try {
            location = inventory.getLocation();
        } catch (Exception ignored) {
        }
        if (location == null) {
            return;
        }

        Block block = location.getBlock();
        if (!block.getType().equals(Material.BREWING_STAND)) {
            return;
        }

        BrewingStand stand = (BrewingStand) block.getState();
        stand.setFuelLevel(20);
    }
}