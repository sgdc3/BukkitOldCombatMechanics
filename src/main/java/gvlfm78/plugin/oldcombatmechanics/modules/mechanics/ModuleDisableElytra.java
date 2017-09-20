package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

public class ModuleDisableElytra extends Module {

    private List<Material> interactiveBlocks;

    public ModuleDisableElytra(OldCombatMechanics plugin) {
        super(plugin, "disable-elytra");

        interactiveBlocks = plugin.getInteractiveBlocks();
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity human = event.getWhoClicked();
        if (!shouldActivate(human)) {
            return;
        }

        if (!(human instanceof Player)) {
            return;
        }

        Player player = (Player) human;

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        InventoryType type = event.getInventory().getType(); // Only if they're in their inventory, not chests etc.
        if (type != InventoryType.CRAFTING && type != InventoryType.PLAYER) {
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack currentItem = event.getCurrentItem();

        if ((cursor != null && cursor.getType() != Material.ELYTRA) && (currentItem != null && currentItem.getType() != Material.ELYTRA)) {
            return;
        }

        if (event.getSlot() == 38) {
            event.setCancelled(true);
            return;
        }

        //Stop shift clicking elytra in
        if (event.getClick() != ClickType.SHIFT_LEFT && event.getClick() != ClickType.SHIFT_RIGHT) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event) {
        if (!shouldActivate(event.getPlayer().getWorld())) {
            return;
        }

        //Must not be able to right click while holding it to wear it
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getMaterial() != Material.ELYTRA) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block != null && interactiveBlocks.contains(block.getType())) {
            return;
        }

        event.setCancelled(true);
    }

    //Make sure they can't click in, shift click, number key, drag in

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (!shouldActivate(event.getWhoClicked().getWorld())) {
            return;
        }

        if (event.getOldCursor() == null || (event.getCursor() != null && event.getCursor().getType() != Material.ELYTRA)) {
            return;
        }

        if (!event.getInventorySlots().contains(38)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (!shouldActivate(world)) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack chestplate = inventory.getChestplate();

        if (chestplate == null || chestplate.getType() != Material.ELYTRA) {
            return;
        }

        inventory.setChestplate(new ItemStack(Material.AIR));

        if (inventory.firstEmpty() != -1) {
            inventory.addItem(chestplate);
        } else {
            world.dropItem(player.getLocation(), chestplate);
        }
    }
}