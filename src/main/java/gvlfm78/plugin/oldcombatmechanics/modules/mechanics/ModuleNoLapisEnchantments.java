package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;

public class ModuleNoLapisEnchantments extends Module {

    private boolean usePermission;

    public ModuleNoLapisEnchantments(OldCombatMechanics plugin) {
        super(plugin, "no-lapis-enchantments");

        usePermission = getConfiguration().getBoolean("usePermission");
    }

    private static ItemStack getLapisItem() {
        Dye dye = new Dye();
        dye.setColor(DyeColor.BLUE);
        return dye.toItemStack(64);
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    private boolean hasPermission(Player player) {
        return !usePermission || player.hasPermission("oldcombatmechanics.nolapis");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEnchant(EnchantItemEvent event) {
        Block block = event.getEnchantBlock();

        if (!shouldActivate(block.getWorld())) {
            return;
        }

        if (!hasPermission(event.getEnchanter())) {
            return;
        }

        EnchantingInventory inventory = (EnchantingInventory) event.getInventory(); //Not checking here because how else would event be fired?
        inventory.setSecondary(getLapisItem());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!shouldActivate(event.getWhoClicked())) {
            return;
        }

        if (!event.getInventory().getType().equals(InventoryType.ENCHANTING)) {
            return;
        }

        if (!hasPermission((Player) event.getWhoClicked())) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item != null && (
                (item.getType() == Material.INK_SACK && event.getRawSlot() == 1) ||
                        (event.getCursor() != null && event.getCursor().getType() == Material.INK_SACK && event.getClick() == ClickType.DOUBLE_CLICK))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!shouldActivate(event.getPlayer())) {
            return;
        }

        Inventory inventory = event.getInventory();
        if (inventory == null || inventory.getType() != InventoryType.ENCHANTING || !hasPermission((Player) event.getPlayer())) {
            return;
        }

        EnchantingInventory enchantingInventory = (EnchantingInventory) inventory;
        enchantingInventory.setSecondary(new ItemStack(Material.AIR));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!shouldActivate(event.getPlayer())) {
            return;
        }

        Inventory inventory = event.getInventory();
        if (inventory == null || inventory.getType() != InventoryType.ENCHANTING || !hasPermission((Player) event.getPlayer())) {
            return;
        }

        EnchantingInventory enchantingInventory = (EnchantingInventory) event.getInventory();
        enchantingInventory.setSecondary(getLapisItem());
    }
}