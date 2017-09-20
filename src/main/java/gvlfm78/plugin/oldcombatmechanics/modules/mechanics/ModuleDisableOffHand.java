package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class ModuleDisableOffHand extends ModuleWithMaterialList {

    public ModuleDisableOffHand(OldCombatMechanics plugin) {
        super(plugin, "disable-offhand");
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (shouldActivate(event.getPlayer()) && shouldBeCancelled(event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!shouldActivate(event.getWhoClicked())) {
            return;
        }

        //Making sure it's a survival player's inventory
        if (event.getInventory().getType() != InventoryType.CRAFTING) {
            return;
        }

        // If they didn't click into the offhand slot, return
        if (event.getSlot() != 40) {
            return;
        }

        if (event.getClick().equals(ClickType.NUMBER_KEY) || shouldBeCancelled(event.getCursor())) {
            event.setResult(Event.Result.DENY);
            event.setCancelled(true); // TODO: are we sure we need to do cancel a denied event?
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!shouldActivate(event.getWhoClicked())) {
            return;
        }

        //Making sure it's a survival player's inventory
        if (event.getInventory().getType() != InventoryType.CRAFTING) {
            return;
        }

        // If they didn't click into the offhand slot, return
        if (!event.getInventorySlots().contains(40)) {
            return;
        }

        if (shouldBeCancelled(event.getOldCursor())) {
            event.setResult(Event.Result.DENY);
            event.setCancelled(true); // TODO: are we sure we need to do cancel a denied event?
        }
    }
}
