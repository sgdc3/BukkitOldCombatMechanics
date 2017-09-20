package gvlfm78.plugin.oldcombatmechanics.modules.mechanics;

import gvlfm78.plugin.oldcombatmechanics.OldCombatMechanics;
import gvlfm78.plugin.oldcombatmechanics.modules.Module;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ModuleAttackCooldown extends Module {

    private double genericAttackSpeed;

    public ModuleAttackCooldown(OldCombatMechanics plugin) {
        super(plugin, "disable-attack-cooldown");

        genericAttackSpeed = getConfiguration().getDouble("generic-attack-speed");
    }

    @Override
    public void onEnable() {
        //Setting correct attack speed and armour values for online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            double attackSpeed = genericAttackSpeed;
            if (!shouldActivate(player)) {
                attackSpeed = 4;
            }

            AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
            double baseValue = attribute.getBaseValue();

            if (baseValue != attackSpeed) {
                attribute.setBaseValue(attackSpeed);
                //player.saveData();
            }
        }
    }

    @Override
    public void onDisable() {
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        double baseValue = attribute.getBaseValue();

        double genericAttackSpeed = this.genericAttackSpeed;
        if (!shouldActivate(player)) {
            genericAttackSpeed = 4; //If getConfiguration is disabled, set attack speed to 1.9 default
        }

        if (baseValue != genericAttackSpeed) {
            attribute.setBaseValue(genericAttackSpeed);
            player.saveData();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        double baseValue = attribute.getBaseValue();

        //If baseValue is not 1.9 default, set it back
        if (baseValue != 4) {
            attribute.setBaseValue(4);
            //player.saveData();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        double baseValue = attribute.getBaseValue();

        double genericAttackSpeed = this.genericAttackSpeed;

        //If getConfiguration is disabled, set attack speed to 1.9 default
        if (!shouldActivate(player)) {
            genericAttackSpeed = 4;
        }

        if (baseValue != genericAttackSpeed) {
            attribute.setBaseValue(genericAttackSpeed);
            //player.saveData();
        }
    }
}
