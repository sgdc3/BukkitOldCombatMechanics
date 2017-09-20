package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import gvlfm78.plugin.oldcombatmechanics.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ModuleSwordBlocking extends Module {

    private static final ItemStack SHIELD = new ItemStack(Material.SHIELD);

    private Map<UUID, ItemStack> storedOffhandItems;
    private Map<UUID, BukkitRunnable> correspondingTasks;

    private List<Material> interactiveBlocks;
    private List<Material> noBlockingItems;
    private double minimumDamage;
    private boolean shieldFullBlock;
    private int restoreDelay;
    private String blockingDamageReduction;
    private boolean blacklist;

    public ModuleSwordBlocking(OldCombatMechanics plugin) {
        super(plugin, "sword-blocking");

        interactiveBlocks = plugin.getInteractiveBlocks();
        minimumDamage = getConfiguration().getDouble("minimumDamage");
        shieldFullBlock = getConfiguration().getBoolean("shieldFullBlock");
        restoreDelay = getConfiguration().getInt("restoreDelay", 40);
        blockingDamageReduction = getConfiguration().getString("blockingDamageReduction", "1");
        blockingDamageReduction = blockingDamageReduction.replaceAll(" ", "");
        blacklist = getConfiguration().getBoolean("blacklist");
        loadNoBlockingItems();
    }

    private void loadNoBlockingItems() {
        noBlockingItems = new ArrayList<>();
        List<String> rawItems = getConfiguration().getStringList("noBlockingItems");
        if (rawItems == null) {
            return;
        }
        for (String itemName : rawItems) {
            Material material = Material.matchMaterial(itemName);
            if (material != null) {
                noBlockingItems.add(material);
            }
        }
    }

    @Override
    public void onEnable() {
        storedOffhandItems = new HashMap<>();
        correspondingTasks = new HashMap<>();
    }

    @Override
    public void onDisable() {
        // Cancel all tasks
        for (BukkitRunnable task : correspondingTasks.values()) {
            task.cancel();
        }
        // Restore all
        restoreAll();
        storedOffhandItems = null;
        correspondingTasks = null;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (action == Action.RIGHT_CLICK_BLOCK && interactiveBlocks.contains(event.getClickedBlock().getType())) {
            return;
        }

        Player player = event.getPlayer();
        if (!shouldActivate(player)) {
            return;
        }
        UUID uuid = player.getUniqueId();

        if (player.isBlocking()) {
            correspondingTasks.get(uuid).cancel();
            correspondingTasks.remove(uuid);
        } else {
            if (!ItemUtils.materialEndsWith(item.getType(), "sword") || hasShield(player)) {
                return;
            }

            PlayerInventory inventory = player.getInventory();
            boolean isANoBlockingItem = noBlockingItems.contains(inventory.getItemInOffHand().getType());
            if (blacklist && isANoBlockingItem || !blacklist && !isANoBlockingItem) {
                return;
            }
            storedOffhandItems.put(uuid, inventory.getItemInOffHand());
            inventory.setItemInOffHand(SHIELD);
        }

        correspondingTasks.put(uuid, scheduleRestore(player));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();

        if (entity == null || !(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;

        if (isBlocking(player.getUniqueId())) {
            //If it's a player blocking
            //Instead of reducing damage to 33% apply config reduction

            double damageReduction = event.getDamage(); //Reducing by this would mean blocking all damage

            if (blockingDamageReduction.matches("\\d{1,3}%")) {
                //Reduce damage by percentage
                int percentage = Integer.parseInt(blockingDamageReduction.replace("%", ""));
                damageReduction = damageReduction * percentage / 100;
            } else if (blockingDamageReduction.matches("\\d+")) {
                //Reduce by specified amount of half-hearts
                damageReduction = Integer.parseInt(blockingDamageReduction);
            } else damageReduction = 0;

            if (damageReduction < 0) {
                damageReduction = 0;
            }

            //Only reduce damage if they were hit head on, i.e. the shield blocked some of the damage
            if (event.getDamage(DamageModifier.BLOCKING) >= 0) {
                return;
            }

            //Also make sure reducing the damage doesn't result in negative damage
            event.setDamage(DamageModifier.BLOCKING, 0);

            if (event.getFinalDamage() >= damageReduction) {
                event.setDamage(DamageModifier.BLOCKING, damageReduction * -1);
            }

            //Make maximum reduction possible be up to amount specified in config

            if (!shieldFullBlock && event.getFinalDamage() < minimumDamage) {
                event.setDamage(minimumDamage);
            }

            debug("Damage reduced by: " + event.getDamage(DamageModifier.BLOCKING), player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        restore(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogout(PlayerQuitEvent event) {
        restore(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        if (!isBlocking(event.getEntity().getUniqueId())) {
            return;
        }

        event.getDrops().replaceAll(item -> {
            if (item.getType().equals(SHIELD.getType())) {
                item = storedOffhandItems.get(uuid);
            }

            return item;
        });

        storedOffhandItems.remove(uuid);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (isBlocking(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();

        if (!isBlocking(player.getUniqueId())) {
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();
        if (cursor != null && cursor.getType() == SHIELD.getType() ||
                current != null && current.getType() == SHIELD.getType()) {
            event.setCancelled(true);
            restore(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDrop(PlayerDropItemEvent e) {
        Item is = e.getItemDrop();

        Player p = e.getPlayer();

        if (isBlocking(p.getUniqueId()) && is.getItemStack().getType() == SHIELD.getType()) {
            e.setCancelled(true);
            restore(p);
        }
    }

    private BukkitRunnable scheduleRestore(final Player player) {
        return scheduleDelayed(new BukkitRunnable() {
            @Override
            public void run() {
                restore(player);
            }
        }, restoreDelay);
    }

    private void restore(Player player) {
        UUID uuid = player.getUniqueId();

        if (!isBlocking(uuid)) {
            return;
        }

        if (player.isBlocking()) { //They are still blocking with the shield so postpone restoring
            postponeRestoring(player);
        } else {
            player.getInventory().setItemInOffHand(storedOffhandItems.get(uuid));
            storedOffhandItems.remove(uuid);
        }
    }

    private void postponeRestoring(Player p) {
        UUID id = p.getUniqueId();
        correspondingTasks.get(id).cancel();
        correspondingTasks.remove(id);
        correspondingTasks.put(id, scheduleRestore(p));
    }

    private void restoreAll() {
        for (UUID uuid : storedOffhandItems.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            player.getInventory().setItemInOffHand(storedOffhandItems.remove(uuid));
        }
    }

    private boolean isBlocking(UUID uuid) {
        return storedOffhandItems.containsKey(uuid);
    }

    private boolean hasShield(Player player) {
        return player.getInventory().getItemInOffHand().getType() == SHIELD.getType();
    }
}
