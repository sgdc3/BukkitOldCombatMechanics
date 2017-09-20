package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ModuleDisableEnderpearlCooldown extends Module {

    public ModuleDisableEnderpearlCooldown(OldCombatMechanics plugin) {
        super(plugin, "disable-enderpearl-cooldown");
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerShoot(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (!shouldActivate(player)) {
            return;
        }

        if (event.getMaterial() != Material.ENDER_PEARL) {
            return;
        }

        event.setCancelled(true);

        EnderPearl pearl = player.launchProjectile(EnderPearl.class);

        pearl.setVelocity(player.getEyeLocation().getDirection().multiply(2));

        GameMode gamemode = player.getGameMode();

        if (gamemode != GameMode.ADVENTURE && gamemode != GameMode.SURVIVAL) {
            return;
        }

        PlayerInventory inventory = player.getInventory();

        boolean isInOffhand = true;
        ItemStack itemInHand = inventory.getItemInOffHand();

        if (itemInHand.getType() != Material.ENDER_PEARL) {
            itemInHand = inventory.getItemInMainHand();
            isInOffhand = false;
        }

        itemInHand.setAmount(itemInHand.getAmount() - 1);

        if (isInOffhand) {
            inventory.setItemInOffHand(itemInHand);
        } else {
            inventory.setItemInMainHand(itemInHand);
        }
    }
}